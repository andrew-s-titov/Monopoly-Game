package com.monopolynew.service;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BankruptcyEvent;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.GameOverEvent;
import com.monopolynew.event.GameStageEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.NewPlayerTurn;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.map.AirportField;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.mapper.GameFieldMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class GameLogicExecutor {

    private final GameEventSender gameEventSender;
    private final GameFieldMapper gameFieldMapper;
    private final GameEventGenerator gameEventGenerator;
    private final GameRepository gameRepository;

    public void sendToJail(Game game, Player player) {
        player.resetDoublets();
        player.imprison();
        changePlayerPosition(player, Rules.JAIL_FIELD_NUMBER);
    }

    public void sendBuyProposal(Game game, Player player, PurchasableField field) {
        var buyerId = player.getId();
        var buyProposal = new BuyProposal(buyerId, field);
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

    public void endTurn(Game game) {
        if (!GameStage.ROLLED_FOR_JAIL.equals(game.getStage())) {
            // as there was no actual turn when turn ended after rolled for jail (tried luck but failed)
            processMortgage(game);
        }
        game.getGameMap().resetPurchaseHistory();

        Player currentPlayer = game.getCurrentPlayer();

        Player nextPlayer;
        if (!currentPlayer.isJustAmnestied() && game.getLastDice() != null && currentPlayer.getDoubletCount() > 0
                && !currentPlayer.isBankrupt() && !currentPlayer.isSkipping() && !currentPlayer.isImprisoned()) {
            nextPlayer = currentPlayer;
        } else {
            nextPlayer = toNextPlayer(game);
        }
        var nextPlayerId = nextPlayer.getId();
        if (nextPlayer.isImprisoned()) {
            changeGameStage(game, GameStage.JAIL_RELEASE_START);
            gameEventSender.sendToAllPlayers(new NewPlayerTurn(nextPlayerId));
            gameEventSender.sendToPlayer(nextPlayerId, new JailReleaseProcessEvent());
        } else {
            changeGameStage(game, GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(new NewPlayerTurn(nextPlayerId));
            gameEventSender.sendToPlayer(nextPlayerId, new TurnStartEvent());
        }
    }

    public void bankruptPlayer(Game game, Player debtor) {
        debtor.goBankrupt();
        gameEventSender.sendToAllPlayers(new BankruptcyEvent(debtor.getId()));
        var moneyStatesToSend = new ArrayList<MoneyState>();
        var playerFieldsLeft = getPlayerFields(game, debtor);
        var checkToPay = game.getCheckToPay();
        if (checkToPay != null) {
            var beneficiary = checkToPay.getBeneficiary();
            var shouldProcessCheck = debtor.equals(checkToPay.getDebtor()) && beneficiary != null;
            if (shouldProcessCheck) {
                var sumToTransfer = Math.min(computePlayerAssets(game, debtor), checkToPay.getDebt());
                if (sumToTransfer > 0) {
                    beneficiary.addMoney(sumToTransfer);
                    moneyStatesToSend.add(MoneyState.fromPlayer(beneficiary));
                }
            }
            game.setCheckToPay(null);
        }
        var playerMoneyLeft = debtor.getMoney();
        if (playerMoneyLeft > 0) {
            debtor.takeMoney(playerMoneyLeft);
            moneyStatesToSend.add(MoneyState.fromPlayer(debtor));
        }
        if (CollectionUtils.isNotEmpty(playerFieldsLeft)) {
            playerFieldsLeft.forEach(field -> {
                field.redeem();
                field.newOwner(null); // property goes to Bank
                removeFieldHouses(field);
            });
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
                .map(PurchasableFieldGroups::getGroupIdByFieldIndex)
                .distinct()
                .map(groupId -> PurchasableFieldGroups.getGroupById(game, groupId))
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
        if (anyFieldFromGroup instanceof StreetField) {
            boolean allGroupHasSameOwner = fieldGroup.size() == ownedFields.size()
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            ownedFields.stream()
                    .map(StreetField.class::cast)
                    .forEach(streetField -> streetField.refreshRent(allGroupHasSameOwner));
        } else if (anyFieldFromGroup instanceof AirportField) {
            ownedFields.stream()
                    .map(AirportField.class::cast)
                    .forEach(airportField -> {
                        var owningPlayer = airportField.getOwner();
                        int ownedBySameOwner = (int) ownedFields.stream()
                                .filter(field -> owningPlayer.equals(field.getOwner()))
                                .count();
                        airportField.refreshRent(ownedBySameOwner);
                    });
        } else if (anyFieldFromGroup instanceof UtilityField) {
            boolean allUtilitiesOwned = fieldGroup.size() == ownedFields.size()
                    && 1 == ownedFields.stream()
                    .map(PurchasableField::getOwner)
                    .distinct()
                    .count();
            ownedFields.stream()
                    .map(UtilityField.class::cast)
                    .forEach(field -> field.refreshRent(allUtilitiesOwned));
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        // NOTE: processing only ownedFields, as for empty fields rent is reset to default
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
            gameEventSender.sendToAllPlayers(new GameOverEvent(winner.getName()));
            finishGame(game);
            return true;
        }
        return false;
    }

    public void changePlayerPosition(Player player, int fieldIndex) {
        player.changePosition(fieldIndex);
        gameEventSender.sendToAllPlayers(new ChipMoveEvent(player.getId(), fieldIndex));
    }

    private Player toNextPlayer(Game game) {
        Player nextPlayer = game.nextPlayer();
        if (nextPlayer.isBankrupt() || nextPlayer.isSkipping()) {
            if (nextPlayer.isSkipping()) {
                gameEventSender.sendToAllPlayers(new ChatMessageEvent(nextPlayer.getName() + " is skipping a turn"));
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

    private void finishGame(Game game) {
        gameRepository.endGame();
        // force to disconnect players?
    }
}
