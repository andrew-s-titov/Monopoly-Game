package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.GameFieldView;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.MortgageChange;
import com.monopolynew.dto.PropertyPrice;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.GameOverEvent;
import com.monopolynew.event.GameStageEvent;
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
        changeGameStage(game, GameStage.BUY_PROPOSAL);
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
        if (field instanceof StreetField streetField) {
            boolean allGroupOwnedByTheSameOwner = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            if (allGroupOwnedByTheSameOwner) {
                fieldGroup.stream()
                        .map(StreetField.class::cast)
                        .forEach(strField -> strField.setNewRent(true));
                newPriceViews = fieldGroup.stream()
                        .map(gameFieldConverter::toView)
                        .toList();
            } else {
                streetField.setNewRent(false);
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else if (field instanceof CompanyField companyField) {
            List<CompanyField> playerCompanyFields = fieldGroup.stream()
                    .filter(f -> !f.isFree())
                    .filter(f -> f.getOwner().equals(field.getOwner()))
                    .map(f -> (CompanyField) f)
                    .toList();
            int ownedByTheSamePlayer = playerCompanyFields.size();
            if (ownedByTheSamePlayer > 1) {
                playerCompanyFields
                        .forEach(compField -> compField.setNewRent(ownedByTheSamePlayer));
                newPriceViews = playerCompanyFields.stream()
                        .map(gameFieldConverter::toView)
                        .toList();
            } else {
                companyField.setNewRent(ownedByTheSamePlayer);
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
                        .toList();
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
        return getPlayerFields(game, player).stream()
                .filter(field -> !field.isMortgaged())
                .map(field -> field instanceof StreetField streetField
                        ? streetField.getHousePrice() * streetField.getHouses() + field.getPrice() / 2
                        : field.getPrice() / 2 )
                .reduce(player.getMoney(), Integer::sum);
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
            // as there was no actual turn when turn ended after rolled for jail (tried luck but failed)
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
            changeGameStage(game, GameStage.JAIL_RELEASE_START);
            gameEventSender.sendToAllPlayers(new JailReleaseProcessEvent(
                    nextPlayerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            changeGameStage(game, GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(new TurnStartEvent(nextPlayerId));
        }
    }

    @Override
    public void bankruptPlayer(Game game, Player debtor) {
        GameStage stage = game.getStage();
        debtor.goBankrupt();
        gameEventSender.sendToAllPlayers(new BankruptcyEvent(debtor.getId()));
        var moneyStatesToSend = new ArrayList<MoneyState>();
        var playerMoneyLeft = debtor.getMoney();
        var playerFieldsLeft = getPlayerFields(game, debtor);
        var playerFieldsToProcess = new ArrayList<>(playerFieldsLeft);
        var shouldProcessFields = !CollectionUtils.isEmpty(playerFieldsLeft);
        if (playerMoneyLeft > 0) {
            debtor.takeMoney(playerMoneyLeft);
            moneyStatesToSend.add(MoneyState.fromPlayer(debtor));
        }
        var checkToPay = game.getCheckToPay();
        if (checkToPay != null) {
            var beneficiary = checkToPay.getBeneficiary();
            var shouldProcessCheck = debtor.equals(checkToPay.getDebtor()) && beneficiary != null
                    && GameStage.AWAITING_PAYMENT.equals(stage);
            var debt = checkToPay.getDebt();
            if (shouldProcessCheck) {
                var sumToTransfer = Math.min(playerMoneyLeft, debt);
                beneficiary.addMoney(sumToTransfer);
                debt = debt - sumToTransfer;
                if (shouldProcessFields && debt > 0) {
                    processFieldsForBeneficiary(beneficiary, debt, playerFieldsLeft, playerFieldsToProcess);
                }
                moneyStatesToSend.add(MoneyState.fromPlayer(beneficiary));
            }
        }
        if (shouldProcessFields) {
            for (PurchasableField field : playerFieldsToProcess) {
                field.redeem();
                field.newOwner(null); // property goes to Bank
                removeFieldHouses(field);
            }
            gameEventSender.sendToAllPlayers(
                    new FieldViewChangeEvent(gameFieldConverter.toListView(playerFieldsLeft)));
        }
        if (!CollectionUtils.isEmpty(moneyStatesToSend)) {
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStatesToSend));
        }
        if (!isGameFinished(game) && debtor.equals(game.getCurrentPlayer())) {
            endTurn(game);
        }
    }

    @Override
    public void changeGameStage(Game game, GameStage newGameStage) {
        game.setStage(newGameStage);
        gameEventSender.sendToAllPlayers(new GameStageEvent(newGameStage));
    }

    private List<PurchasableField> getPlayerFields(Game game, Player player) {
        return game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .filter(field -> player.equals(field.getOwner()))
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

    private void removeFieldHouses(PurchasableField field) {
        if (field instanceof StreetField streetField && streetField.getHouses() > 0) {
            streetField.sellAllHouses();
        }
    }

    private int getFieldMortgagePrice(PurchasableField field) {
        return field.getPrice() / 2;
    }

    private void processFieldsForBeneficiary(Player beneficiary, int debt,
                                             List<PurchasableField> playerFieldsLeft,
                                             List<PurchasableField> playerFieldsToProcess) {
        var debtorPropertyPrice = computeDebtorPropertyPrice(playerFieldsLeft);

        if (debtorPropertyPrice.getTotal() <= debt) {
            for (PurchasableField field : playerFieldsLeft) {
                field.newOwner(beneficiary);
                removeFieldHouses(field);
                playerFieldsToProcess.remove(field);
            }
        } else {
            var housePriceToTransfer = Math.min(debtorPropertyPrice.getHousesPrice(), debt);
            debt = debt - housePriceToTransfer;
            var fieldsIterator = playerFieldsLeft.iterator();
            while (debt > 0 && fieldsIterator.hasNext()) {
                var field = fieldsIterator.next();
                removeFieldHouses(field);
                var price = field.isMortgaged() ? getFieldMortgagePrice(field) : field.getPrice();
                if (debt >= price) {
                    debt = debt - price;
                    field.newOwner(beneficiary);
                    playerFieldsToProcess.remove(field);
                } else {
                    beneficiary.addMoney(debt);
                    debt = 0;
                }
            }
        }
    }

    private PropertyPrice computeDebtorPropertyPrice(List<PurchasableField> playerFieldsLeft) {
        var housesPrice = playerFieldsLeft.stream()
                .filter(StreetField.class::isInstance)
                .map(StreetField.class::cast)
                .map(street -> street.getHousePrice() * street.getHouses())
                .reduce(0, Integer::sum);
        var fieldsPrice = playerFieldsLeft.stream()
                .map(field -> field.isMortgaged()
                        ? getFieldMortgagePrice(field)
                        : field.getPrice())
                .reduce(0, Integer::sum);
        return PropertyPrice.builder()
                .housesPrice(housesPrice)
                .fieldsPrice(fieldsPrice)
                .build();
    }
}
