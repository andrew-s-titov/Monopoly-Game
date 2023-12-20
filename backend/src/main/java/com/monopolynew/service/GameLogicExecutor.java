package com.monopolynew.service;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.PropertyPrice;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.GameOverEvent;
import com.monopolynew.event.GameStageEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.mapper.GameFieldMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class GameLogicExecutor {

    private final GameEventSender gameEventSender;
    private final GameFieldMapper gameFieldMapper;
    private final GameEventGenerator gameEventGenerator;

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

    public void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason) {
        player.resetDoublets();
        player.imprison();
        gameEventSender.sendToAllPlayers(
                new ChatMessageEvent(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, Rules.JAIL_FIELD_NUMBER, false);
        endTurn(game);
    }

    public void sendBuyProposal(Game game, Player player, PurchasableField field, boolean payable) {
        var buyerId = player.getId();
        var buyProposal = new BuyProposal(buyerId, field, payable);
        game.setBuyProposal(buyProposal);
        changeGameStage(game, GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(buyerId, gameEventGenerator.buyProposalEvent(buyProposal));
    }

    public void doBuyField(Game game, PurchasableField field, int price, UUID buyerId) {
        var buyer = game.getPlayerById(buyerId);
        field.newOwner(buyer);
        buyer.takeMoney(price);
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                String.format("%s is buying %s for $%s", buyer.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(buyer))));

        var processedOwnedFields = processOwnershipChange(game, field);
        List<GameFieldState> newFieldStates = gameFieldMapper.toStateList(processedOwnedFields);
        gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(newFieldStates));
    }

    public int computePlayerAssets(Game game, Player player) {
        return getPlayerFields(game, player).stream()
                .filter(field -> !field.isMortgaged())
                .map(field -> field instanceof StreetField streetField
                        ? streetField.getHousePrice() * streetField.getHouses() + field.getPrice() / 2
                        : field.getPrice() / 2)
                .reduce(player.getMoney(), Integer::sum);
    }

    public int computeNewPlayerPosition(Player player, DiceResult diceResult) {
        int result = player.getPosition() + diceResult.getSum();
        boolean newCircle = result > Rules.LAST_FIELD_INDEX;
        return newCircle ? result - Rules.NUMBER_OF_FIELDS : result;
    }

    public void endTurn(Game game) {
        if (!GameStage.ROLLED_FOR_JAIL.equals(game.getStage())) {
            // as there was no actual turn when turn ended after rolled for jail (tried luck but failed)
            processMortgage(game);
        }
        game.getGameMap().resetPurchaseHistory();

        Player currentPlayer = game.getCurrentPlayer();

        Player nextPlayer;
        if (!currentPlayer.isJustAmnestied() && game.getLastDice() != null && game.getLastDice().isDoublet()
                && !currentPlayer.isBankrupt() && !currentPlayer.isSkipping() && !currentPlayer.isImprisoned()) {
            nextPlayer = currentPlayer;
        } else {
            nextPlayer = toNextPlayer(game);
        }
        var nextPlayerId = nextPlayer.getId();
        if (nextPlayer.isImprisoned()) {
            changeGameStage(game, GameStage.JAIL_RELEASE_START);
            gameEventSender.sendToAllPlayers(new JailReleaseProcessEvent(nextPlayerId));
        } else {
            changeGameStage(game, GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(new TurnStartEvent(nextPlayerId));
        }
    }

    public void bankruptPlayer(Game game, Player debtor) {
        debtor.goBankrupt();
        gameEventSender.sendToAllPlayers(new BankruptcyEvent(debtor.getId()));
        var moneyStatesToSend = new ArrayList<MoneyState>();
        var playerMoneyLeft = debtor.getMoney();
        var playerFieldsLeft = getPlayerFields(game, debtor);
        var playerFieldsToProcess = new ArrayList<>(playerFieldsLeft);
        var shouldProcessFields = CollectionUtils.isNotEmpty(playerFieldsLeft);
        if (playerMoneyLeft > 0) {
            debtor.takeMoney(playerMoneyLeft);
            moneyStatesToSend.add(MoneyState.fromPlayer(debtor));
        }
        var checkToPay = game.getCheckToPay();
        if (checkToPay != null) {
            var beneficiary = checkToPay.getBeneficiary();
            var shouldProcessCheck = debtor.equals(checkToPay.getDebtor()) && beneficiary != null;
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
            game.setCheckToPay(null);
        }
        if (shouldProcessFields) {
            for (PurchasableField field : playerFieldsToProcess) {
                field.redeem();
                field.newOwner(null); // property goes to Bank
                removeFieldHouses(field);
            }
            gameEventSender.sendToAllPlayers(
                    new FieldStateChangeEvent(gameFieldMapper.toStateList(playerFieldsLeft)));
        }
        if (CollectionUtils.isNotEmpty(moneyStatesToSend)) {
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStatesToSend));
        }
        if (!isGameFinished(game) && debtor.equals(game.getCurrentPlayer())) {
            endTurn(game);
        }
    }

    public void changeGameStage(Game game, GameStage newGameStage) {
        game.setStage(newGameStage);
        gameEventSender.sendToAllPlayers(new GameStageEvent(newGameStage));
    }

    public int getFieldMortgagePrice(PurchasableField field) {
        return field.getPrice() / 2;
    }

    /**
     * Refreshes rents for all owned fields in the group, to which passed fields belong
     *
     * @param game          - game to which fields belong
     * @param changedFields - fields for which owner changed
     * @return list of all owned fields where rent refresh is processed (related to passed fields by group)
     */
    public List<PurchasableField> processOwnershipChange(Game game, Collection<PurchasableField> changedFields) {
        return changedFields.stream()
                .map(GameField::getId)
                .map(id -> PurchasableFieldGroups.getGroupByFieldIndex(game, id))
                .map(this::processOwnershipChangeForGroup)
                .flatMap(List::stream)
                .toList();
    }

    public List<PurchasableField> processOwnershipChange(Game game, PurchasableField field) {
        List<PurchasableField> fieldGroup = PurchasableFieldGroups.getGroupByFieldIndex(game, field.getId());
        return processOwnershipChangeForGroup(fieldGroup);
    }

    private List<PurchasableField> processOwnershipChangeForGroup(List<PurchasableField> fieldGroup) {
        var ownedFields = fieldGroup.stream()
                .filter(field -> !field.isFree())
                .toList();
        var anyFieldFromGroup = fieldGroup.get(0);
        Consumer<List<PurchasableField>> fieldProcessor;
        if (anyFieldFromGroup instanceof StreetField) {
            boolean allGroupHasSameOwner = fieldGroup.size() == ownedFields.size()
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            fieldProcessor = group -> group.stream()
                    .map(StreetField.class::cast)
                    .forEach(streetField -> streetField.refreshRent(allGroupHasSameOwner));
        } else if (anyFieldFromGroup instanceof CompanyField) {
            fieldProcessor = group -> group.stream()
                    .map(CompanyField.class::cast)
                    .forEach(companyField -> {
                        var owningPlayer = companyField.getOwner();
                        int ownedBySameOwner = (int) ownedFields.stream()
                                .filter(field -> owningPlayer.equals(field.getOwner()))
                                .count();
                        companyField.refreshRent(ownedBySameOwner);
                    });
        } else if (anyFieldFromGroup instanceof UtilityField) {
            boolean allUtilitiesOwned = fieldGroup.size() == ownedFields.size()
                    && 1 == ownedFields.stream()
                    .map(PurchasableField::getOwner)
                    .distinct()
                    .count();
            fieldProcessor = group -> group.stream()
                    .map(UtilityField.class::cast)
                    .forEach(field -> field.refreshRent(allUtilitiesOwned));
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        // NOTE: processing only ownedFields, as for empty fields rent is reset to default
        fieldProcessor.accept(ownedFields);
        return ownedFields;
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
        var nonBankruptPlayers = players.stream()
                .filter(p -> !p.isBankrupt())
                .distinct()
                .toList();
        if (nonBankruptPlayers.size() == 1) {
            Player winner = nonBankruptPlayers.get(0);
            game.finishGame();
            gameEventSender.sendToAllPlayers(new GameOverEvent(winner.getName()));
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
        List<PurchasableField> fieldsWithDecreasedMortgage = new ArrayList<>(30);
        List<PurchasableField> fieldsLost = new ArrayList<>(30);
        game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .filter(PurchasableField::isMortgaged)
                .forEach(field -> {
                    if (!field.isMortgagedDuringThisTurn() && field.getOwner().equals(currentPlayer)) {
                        int mortgageTurns = field.decreaseMortgageTurns();
                        if (mortgageTurns == 0) {
                            field.newOwner(null);
                            fieldsLost.add(field);
                        } else {
                            fieldsWithDecreasedMortgage.add(field);
                        }
                    }
                });
        List<PurchasableField> processedOwnedFields = processOwnershipChange(game, fieldsLost);

        var changedFieldStates = Stream.of(fieldsWithDecreasedMortgage, fieldsLost, processedOwnedFields)
                .flatMap(List::stream)
                .distinct()
                .map(gameFieldMapper::toState)
                .toList();
        if (CollectionUtils.isNotEmpty(changedFieldStates)) {
            gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(changedFieldStates));
        }
    }

    private void removeFieldHouses(PurchasableField field) {
        if (field instanceof StreetField streetField && streetField.getHouses() > 0) {
            streetField.removeHouses();
        }
    }

    private void processFieldsForBeneficiary(Player beneficiary, int debt,
                                             List<PurchasableField> playerFieldsLeft,
                                             List<PurchasableField> playerFieldsToProcess) {
        var debtorPropertyPrice = computeDebtorPropertyPrice(playerFieldsLeft);

        if (debtorPropertyPrice.getTotal() <= debt) {
            for (PurchasableField field : playerFieldsLeft) {
                field.newOwner(beneficiary);
                removeFieldHouses(field);
            }
            playerFieldsToProcess.clear();
        } else {
            var housePriceToTransfer = Math.min(debtorPropertyPrice.getHousesPrice(), debt);
            var remainingDebt = debt - housePriceToTransfer;
            var fieldsIterator = playerFieldsLeft.iterator();
            while (remainingDebt > 0 && fieldsIterator.hasNext()) {
                var field = fieldsIterator.next();
                removeFieldHouses(field);
                var fieldPrice = field.isMortgaged() ? getFieldMortgagePrice(field) : field.getPrice();
                if (remainingDebt >= fieldPrice) {
                    remainingDebt = remainingDebt - fieldPrice;
                    field.newOwner(beneficiary); // street instead of money equivalent to street's price
                    playerFieldsToProcess.remove(field);
                } else {
                    beneficiary.addMoney(remainingDebt); // partial money sum instead of whole street
                    remainingDebt = 0;
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
