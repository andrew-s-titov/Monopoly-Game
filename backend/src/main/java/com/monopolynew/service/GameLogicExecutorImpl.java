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
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.mapper.GameFieldMapper;
import com.monopolynew.service.api.GameEventGenerator;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.service.api.GameLogicExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GameLogicExecutorImpl implements GameLogicExecutor {

    private final GameEventSender gameEventSender;
    private final GameFieldMapper gameFieldMapper;
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
        var buyerId = player.getId();
        var buyProposal = new BuyProposal(buyerId, field, payable);
        game.setBuyProposal(buyProposal);
        changeGameStage(game, GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(buyerId, gameEventGenerator.buyProposalEvent(buyProposal));
    }

    @Override
    public void doBuyField(Game game, PurchasableField field, int price, UUID buyerId) {
        var buyer = game.getPlayerById(buyerId);
        field.newOwner(buyer);
        buyer.takeMoney(price);
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                String.format("%s is buying %s for $%s", buyer.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(buyer))));

        List<GameFieldState> newPriceStates;
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
                newPriceStates = gameFieldMapper.toStateList(fieldGroup);
            } else {
                streetField.setNewRent(false);
                newPriceStates = Collections.singletonList(gameFieldMapper.toState(field));
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
                newPriceStates = gameFieldMapper.toStateList(playerCompanyFields);
            } else {
                companyField.setNewRent(ownedByTheSamePlayer);
                newPriceStates = Collections.singletonList(gameFieldMapper.toState(field));
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
                newPriceStates = gameFieldMapper.toStateList(fieldGroup);
            } else {
                newPriceStates = Collections.singletonList(gameFieldMapper.toState(field));
            }
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(newPriceStates));
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

    @Override
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

    @Override
    public void changeGameStage(Game game, GameStage newGameStage) {
        game.setStage(newGameStage);
        gameEventSender.sendToAllPlayers(new GameStageEvent(newGameStage));
    }

    @Override
    public int getFieldMortgagePrice(PurchasableField field) {
        return field.getPrice() / 2;
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
        List<GameFieldState> updatedFieldStates = new ArrayList<>(30);
        game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .filter(PurchasableField::isMortgaged)
                .forEach(field -> {
                    if (!field.isMortgagedDuringThisTurn() && field.getOwner().equals(currentPlayer)) {
                        int mortgageTurns = field.decreaseMortgageTurns();
                        if (mortgageTurns == 0) {
                            field.newOwner(null);
                        }
                        updatedFieldStates.add(gameFieldMapper.toState(field));
                    }
                });
        if (CollectionUtils.isNotEmpty(updatedFieldStates)) {
            gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(updatedFieldStates));
        }
    }

    private void removeFieldHouses(PurchasableField field) {
        if (field instanceof StreetField streetField && streetField.getHouses() > 0) {
            streetField.sellAllHouses();
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
