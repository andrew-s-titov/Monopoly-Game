package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.GameFieldView;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.MortgageChange;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.GameOverEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.MortgageChangeEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameFieldConverter;
import com.monopolynew.service.GameLogicExecutor;
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
    private final GameEventGenerator gameEventGenerator;

    @Override
    public void movePlayer(Game game, Player player, int newPositionIndex, boolean forward) {
        int currentPosition = player.getPosition();
        changePlayerPosition(player, newPositionIndex, true);
        if (forward && newPositionIndex < currentPosition) {
            player.addMoney(Rules.CIRCLE_MONEY);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(
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
                new ChatMessageEvent(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, Rules.JAIL_FIELD_NUMBER, false);
        endTurn(game);
    }

    @Override
    public void sendBuyProposal(Game game, Player player, PurchasableField field, boolean payable) {
        String buyerId = player.getId();
        var buyProposal = new BuyProposal(buyerId, field, payable);
        game.setBuyProposal(buyProposal);
        game.setStage(GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(buyerId, gameEventGenerator.newBuyProposalEvent(buyProposal));
    }

    @Override
    public void doBuyField(Game game, PurchasableField field, int price, String buyerId) {
        var buyer = game.getPlayerById(buyerId);
        field.newOwner(buyer);
        buyer.takeMoney(price);
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                String.format("%s is buying %s for $%s", buyer.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(buyer))));

        List<GameFieldView> newPriceViews;
        List<PurchasableField> fieldGroup = PurchasableFieldGroups.getGroupByFieldIndex(game, field.getId());
        if (field instanceof StreetField) {
            boolean allGroupOwnedByTheSameOwner = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            if (allGroupOwnedByTheSameOwner) {
                fieldGroup.stream()
                        .map(StreetField.class::cast)
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
                    .toList();
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
                    .allMatch(f -> buyer.equals(f.getOwner()));
            if (increasedMultiplier) {
                fieldGroup.stream()
                        .map(UtilityField.class::cast)
                        .forEach(UtilityField::increaseMultiplier);
                newPriceViews = fieldGroup.stream()
                        .map(UtilityField.class::cast)
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
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .filter(field -> player.equals(field.getOwner()))
                .filter(field -> !field.isMortgaged())
                .toList();
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
        boolean newCircle = result > Rules.LAST_FIELD_INDEX;
        return newCircle ? result - Rules.NUMBER_OF_FIELDS : result;
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
        String nextPlayerId = nextPlayer.getId();
        if (nextPlayer.isImprisoned()) {
            game.setStage(GameStage.JAIL_RELEASE_START);
            gameEventSender.sendToAllPlayers(new JailReleaseProcessEvent(
                    nextPlayerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(new TurnStartEvent(nextPlayerId));
        }
    }

    @Override
    public void bankruptPlayer(Game game, Player player, Integer remainingAssets) {
        GameStage stage = game.getStage();
        player.goBankrupt();
        gameEventSender.sendToAllPlayers(new BankruptcyEvent(player.getId()));
        var checkToPay = game.getCheckToPay();
        var moneyStatesToSend = new ArrayList<MoneyState>();
        var playerMoneyLeft = player.getMoney();
        if (playerMoneyLeft > 0) {
            player.takeMoney(playerMoneyLeft);
            moneyStatesToSend.add(MoneyState.fromPlayer(player));
        }
        Player currentPlayer = game.getCurrentPlayer();
        if (checkToPay != null) {
            var beneficiary = checkToPay.getBeneficiary();
            var sumToPay = checkToPay.getSum();
            if (checkToPay.getPlayer().equals(player) && beneficiary != null && GameStage.AWAITING_PAYMENT.equals(stage)) {
                remainingAssets = remainingAssets == null ? computePlayerAssets(game, player) : remainingAssets;
                beneficiary.addMoney(remainingAssets > sumToPay ? sumToPay : remainingAssets);
                moneyStatesToSend.add(MoneyState.fromPlayer(beneficiary));
            }
        }
        if (!CollectionUtils.isEmpty(moneyStatesToSend)) {
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStatesToSend));
        }
        var playerPurchasableFields = getPlayerFields(game, player);
        if (!CollectionUtils.isEmpty(playerPurchasableFields)) {
            playerPurchasableFields.forEach(field -> {
                field.newOwner(null); // all property goes to the 'bank'
                if (field.isMortgaged()) {
                    field.redeem();
                }
                if (field instanceof StreetField && ((StreetField) field).getHouses() > 0) {
                    ((StreetField) field).sellAllHouses();
                }
            });
            gameEventSender.sendToAllPlayers(
                    new FieldViewChangeEvent(gameFieldConverter.toListView(playerPurchasableFields)));
        }
        if (!isGameFinished(game) && player.equals(currentPlayer)) {
            endTurn(game);
        }
    }

    private List<PurchasableField> getPlayerFields(Game game, Player player) {
        return game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .filter(field -> !field.isFree())
                .filter(field -> field.getOwner().equals(player))
                .toList();
    }

    private boolean isGameFinished(Game game) {
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
            gameEventSender.sendToAllPlayers(new GameOverEvent(winner.getId(), winner.getName()));
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
                gameEventSender.sendToAllPlayers(new ChatMessageEvent(nextPlayer.getName() + " is skipping his/her turn"));
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
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
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
