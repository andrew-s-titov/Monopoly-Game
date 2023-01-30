package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.GameFieldView;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.MortgageChange;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.GameOverEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.MortgageChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameFieldConverter;
import com.monopolynew.service.GameLogicExecutor;
import com.monopolynew.service.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class GameLogicExecutorImpl implements GameLogicExecutor {

    private final GameEventSender gameEventSender;
    private final GameFieldConverter gameFieldConverter;

    @Override
    public void movePlayer(Game game, Player player, int newPositionIndex, boolean forward) {
        int currentPosition = player.getPosition();
        changePlayerPosition(player, newPositionIndex, true);
        if (forward && newPositionIndex < currentPosition) {
            player.addMoney(Rules.CIRCLE_MONEY);
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                    String.format("%s received $%s for starting a new circle",
                            player.getName(), Rules.CIRCLE_MONEY)));
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }
    }

    @Override
    public void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason) {
        player.resetDoublets();
        player.imprison();
        gameEventSender.sendToAllPlayers(
                SystemMessageEvent.text(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, GameMap.JAIL_FIELD_NUMBER, false);
        endTurn(game);
    }

    @Override
    public void sendBuyProposal(Game game, Player player, PurchasableField field) {
        var buyProposal = new BuyProposal(player, field);
        game.setBuyProposal(buyProposal);
        game.setStage(GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(player.getId(), BuyProposalEvent.fromProposal(buyProposal));
    }

    @Override
    public void doBuyField(Game game, PurchasableField field, int price, Player player) {
        field.newOwner(player);
        player.takeMoney(price);
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s is buying %s for $%s", player.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(player))));

        int fieldGroupId = field.getGroupId();
        List<GameFieldView> newPriceViews;
        List<PurchasableField> fieldGroup = game.getGameMap().getGroup(fieldGroupId);
        if (field instanceof StreetField) {
            boolean allGroupOwnedByTheSameOwner = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            if (allGroupOwnedByTheSameOwner) {
                fieldGroup.stream()
                        .map(streetField -> (StreetField) streetField)
                        .forEach(streetField -> streetField.setNewRent(true));
                newPriceViews = fieldGroup.stream()
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                ((StreetField) field).setNewRent(false);
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else if (field instanceof CompanyField) {
            List<CompanyField> playerCompanyFields = fieldGroup.stream()
                    .filter(f -> !f.isFree())
                    .filter(f -> f.getOwner().equals(field.getOwner()))
                    .map(f -> (CompanyField) f)
                    .collect(Collectors.toList());
            int ownedByTheSamePlayer = playerCompanyFields.size();
            if (ownedByTheSamePlayer > 1) {
                playerCompanyFields
                        .forEach(companyField -> companyField.setNewRent(ownedByTheSamePlayer));
                newPriceViews = playerCompanyFields.stream()
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                ((CompanyField) field).setNewRent(ownedByTheSamePlayer);
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else if (field instanceof UtilityField) {
            boolean increasedMultiplier = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && fieldGroup.stream()
                    .allMatch(f -> player.equals(f.getOwner()));
            if (increasedMultiplier) {
                fieldGroup.stream()
                        .map(utilityField -> (UtilityField) utilityField)
                        .forEach(UtilityField::increaseMultiplier);
                newPriceViews = fieldGroup.stream()
                        .map(utilityField -> (UtilityField) utilityField)
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(newPriceViews));
    }

    @Override
    public int computePlayerAssets(Game game, Player player) {
        int assetSum = player.getMoney();
        List<PurchasableField> countedFields = game.getGameMap().getFields().stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(field -> player.equals(field.getOwner()))
                .filter(field -> !field.isMortgaged())
                .collect(Collectors.toList());
        for (PurchasableField field : countedFields) {
            if (field instanceof StreetField) {
                int houses = ((StreetField) field).getHouses();
                int housePrice = ((StreetField) field).getHousePrice();
                assetSum += housePrice * houses;
            }
            int price = field.getPrice();
            assetSum += price / 2;
        }
        return assetSum;
    }

    @Override
    public int computeNewPlayerPosition(Player player, DiceResult diceResult) {
        int result = player.getPosition() + diceResult.getSum();
        boolean newCircle = result > GameMap.LAST_FIELD_INDEX;
        return newCircle ? result - GameMap.NUMBER_OF_FIELDS : result;
    }

    @Override
    public void endTurn(Game game) {
        if (!GameStage.ROLLED_FOR_JAIL.equals(game.getStage())) {
            // as there was no actual turn when turn ended after rolled for jail (tried luck but hot nota a doublet)
            processMortgage(game);
        }
        game.getGameMap().resetPurchaseHistory();

        Player currentPlayer = game.getCurrentPlayer();

        Player nextPlayer;
        if (!currentPlayer.isAmnestied() && game.getLastDice() != null && game.getLastDice().isDoublet()
                && !currentPlayer.isBankrupt() && !currentPlayer.isSkipping() && !currentPlayer.isImprisoned()) {
            nextPlayer = currentPlayer;
        } else {
            nextPlayer = toNextPlayer(game);
        }
        if (nextPlayer.isImprisoned()) {
            game.setStage(GameStage.JAIL_RELEASE_START);
            String nextPlayerId = nextPlayer.getId();
            gameEventSender.sendToAllPlayers(new JailReleaseProcessEvent(
                    nextPlayerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(nextPlayer));
        }
    }

    @Override
    public void bankruptPlayer(Game game, Player player) {
        GameStage stage = game.getStage();
        Player currentPlayer = game.getCurrentPlayer();
        player.goBankrupt();
        gameEventSender.sendToAllPlayers(BankruptcyEvent.forPlayer(player));
        if (player.equals(currentPlayer) && GameStage.AWAITING_PAYMENT.equals(stage) && game.getCheckToPay().getBeneficiary() != null) {
            Player beneficiary = game.getCheckToPay().getBeneficiary();
            int playerMoneyLeft = player.getMoney();
            if (playerMoneyLeft > 0) {
                beneficiary.addMoney(playerMoneyLeft);
                player.takeMoney(playerMoneyLeft);
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        List.of(MoneyState.fromPlayer(beneficiary), MoneyState.fromPlayer(player))));
            }
            List<PurchasableField> playerFields = getPlayerFields(game, player);
            playerFields.forEach(field -> field.newOwner(beneficiary));
            if (!CollectionUtils.isEmpty(playerFields)) {
                gameEventSender.sendToAllPlayers(
                        new FieldViewChangeEvent(gameFieldConverter.toListView(playerFields)));
            }
        } else {
            bankruptForState(game, player);
        }

        if (!checkIfGameEnded(game) && player.equals(currentPlayer)) {
            endTurn(game);
        }
    }

    private List<PurchasableField> getPlayerFields(Game game, Player player) {
        return game.getGameMap().getFields().stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(field -> !field.isFree())
                .filter(field -> field.getOwner().equals(player))
                .collect(Collectors.toList());
    }

    private void bankruptForState(Game game, Player player) {
        int playerMoneyLeft = player.getMoney();
        if (playerMoneyLeft > 0) {
            player.takeMoney(playerMoneyLeft);
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }
        List<PurchasableField> playerFields = getPlayerFields(game, player);
        playerFields.forEach(field -> {
            field.newOwner(null);
            if (field.isMortgaged()) {
                field.redeem();
            }
            if (field instanceof StreetField) {
                var streetField = (StreetField) field;
                if (streetField.getHouses() > 0) {
                    streetField.sellAllHouses();
                }
            }
        });
        if (!CollectionUtils.isEmpty(playerFields)) {
            gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(gameFieldConverter.toListView(playerFields)));
        }
    }

    private boolean checkIfGameEnded(Game game) {
        Collection<Player> players = game.getPlayers();
        long nonBankruptPlayers = players.stream()
                .filter(p -> !p.isBankrupt())
                .distinct()
                .count();
        if (nonBankruptPlayers == 1) {
            Player winner = players.stream()
                    .filter(p -> !p.isBankrupt())
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("failed to get last non-bankrupt player"));
            game.finishGame();
            gameEventSender.sendToAllPlayers(GameOverEvent.withWinner(winner));
            // TODO: close websocket connection for all players
            // gameEventSender.closeExchangeChannel();
            return true;
        }
        return false;
    }

    private void changePlayerPosition(Player player, int fieldIndex, boolean needAfterMoveCall) {
        player.changePosition(fieldIndex);
        gameEventSender.sendToAllPlayers(new ChipMoveEvent(player.getId(), fieldIndex, needAfterMoveCall));
    }

    private Player toNextPlayer(Game game) {
        Player nextPlayer = game.nextPlayer();
        if (nextPlayer.isBankrupt() || nextPlayer.isSkipping()) {
            if (nextPlayer.isSkipping()) {
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(nextPlayer.getName() + " is skipping his/her turn"));
                nextPlayer.skip();
            }
            nextPlayer = toNextPlayer(game);
        }
        return nextPlayer;
    }

    private void processMortgage(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        List<MortgageChange> mortgageChanges = new ArrayList<>(30);
        List<GameFieldView> fieldViews = new ArrayList<>(30);
        game.getGameMap().getFields().stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(PurchasableField::isMortgaged)
                .forEach(field -> {
                    if (!field.isMortgagedDuringThisTurn() && field.getOwner().equals(currentPlayer)) {
                        int mortgageTurns = field.decreaseMortgageTurns();
                        mortgageChanges.add(new MortgageChange(field.getId(), field.getMortgageTurnsLeft()));
                        if (mortgageTurns == 0) {
                            field.newOwner(null);
                            fieldViews.add(gameFieldConverter.toView(field));
                        }
                    }
                });
        if (!CollectionUtils.isEmpty(mortgageChanges)) {
            gameEventSender.sendToAllPlayers(new MortgageChangeEvent(mortgageChanges));
        }
        if (!CollectionUtils.isEmpty(fieldViews)) {
            gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(fieldViews));
        }
    }
}
