package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.MortgageChange;
import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.MortgageChangeEvent;
import com.monopolynew.event.StreetHouseAmountEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.FieldManagementService;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameFieldConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Component
public class FieldManagementServiceImpl implements FieldManagementService {

    private final GameEventSender gameEventSender;
    private final GameFieldConverter gameFieldConverter;

    @Override
    public List<FieldManagementAction> availableManagementActions(Game game, int fieldIndex, String playerId) {
        checkFieldExists(fieldIndex);
        GameField field = game.getGameMap().getField(fieldIndex);
        List<FieldManagementAction> actions = new ArrayList<>(FieldManagementAction.values().length);
        actions.add(FieldManagementAction.INFO);
        if (field instanceof PurchasableField && !managementNotAvailable(game.getStage())) {
            var purchasableField = (PurchasableField) field;
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer.getId().equals(playerId) && !purchasableField.isFree() && currentPlayer.equals(purchasableField.getOwner())) {
                if (redemptionAvailable(game, purchasableField)) {
                    actions.add(FieldManagementAction.REDEEM);
                } else if (mortgageAvailable(game, purchasableField)) {
                    actions.add(FieldManagementAction.MORTGAGE);
                }
                if (purchasableField instanceof StreetField) {
                    var street = (StreetField) purchasableField;
                    if (houseSaleAvailable(game, street)) {
                        actions.add(FieldManagementAction.SELL_HOUSE);
                    }
                    if (housePurchaseAvailable(game, currentPlayer, street)) {
                        actions.add(FieldManagementAction.BUY_HOUSE);
                    }
                }
            }
        }
        return actions;
    }

    @Override
    public Pair<Integer, Integer> mortgageField(Game game, int fieldIndex, String playerId) {
        return doFieldManagement(game, playerId, fieldIndex, (g, f) -> {
            if (mortgageAvailable(g, f)) {
                Player currentPlayer = g.getCurrentPlayer();
                f.mortgage();
                currentPlayer.addMoney(f.getPrice() / 2);
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                gameEventSender.sendToAllPlayers(new MortgageChangeEvent(
                        Collections.singletonList(new MortgageChange(f.getId(), f.getMortgageTurnsLeft()))));
                return;
            }
            throw new IllegalStateException("Cannot mortgage street with houses");
        });
    }

    @Override
    public Pair<Integer, Integer> redeemMortgagedProperty(Game game, int fieldIndex, String playerId) {
        return doFieldManagement(game, playerId, fieldIndex, (g, f) -> {
            if (redemptionAvailable(g, f)) {
                Player currentPlayer = g.getCurrentPlayer();
                f.redeem();
                currentPlayer.takeMoney(getPropertyRedemptionValue(f));
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                gameEventSender.sendToAllPlayers(new MortgageChangeEvent(
                        Collections.singletonList(new MortgageChange(f.getId(), 0))));
                gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(
                        Collections.singletonList(gameFieldConverter.toView(f))));
                return;
            }
            throw new IllegalStateException("Cannot redeem property - not enough money");
        });
    }

    @Override
    public Pair<Integer, Integer> buyHouse(Game game, int fieldIndex, String playerId) {
        return doFieldManagement(game, playerId, fieldIndex, (aGame, field) -> {
            if (field instanceof StreetField) {
                var streetField = (StreetField) field;
                Player currentPlayer = aGame.getCurrentPlayer();
                if (housePurchaseAvailable(aGame, currentPlayer, streetField)) {
                    streetField.addHouse();
                    aGame.getGameMap().setPurchaseMadeFlag(streetField.getGroupId());
                    streetField.setNewRent(true);
                    currentPlayer.takeMoney(streetField.getHousePrice());
                    notifyAfterHouseManagementChange(currentPlayer, streetField);
                    return;
                }
            }
            throw new IllegalStateException("Cannot buy a house for this property field");
        });
    }

    @Override
    public Pair<Integer, Integer> sellHouse(Game game, int fieldIndex, String playerId) {
        return doFieldManagement(game, playerId, fieldIndex, (aGame, field) -> {
            if (field instanceof StreetField) {
                var streetField = (StreetField) field;
                if (houseSaleAvailable(aGame, streetField)) {
                    streetField.sellHouse();
                    streetField.setNewRent(true);
                    Player currentPlayer = aGame.getCurrentPlayer();
                    currentPlayer.addMoney(streetField.getHousePrice());
                    notifyAfterHouseManagementChange(currentPlayer, streetField);
                    return;
                }
            }
            throw new IllegalStateException("Cannot sell a house on this property field");
        });
    }

    @Override
    public boolean housePurchaseAvailable(Game game, Player player, StreetField streetField) {
        if (streetField.isMortgaged()) {
            return false;
        }
        int currentNumberOfHouses = streetField.getHouses();
        if (currentNumberOfHouses < Rules.MAX_HOUSES_ON_STREET && player.getMoney() >= streetField.getHousePrice()) {
            int streetGroupId = streetField.getGroupId();
            if (game.getGameMap().isPurchaseMadeForGroup(streetGroupId)) {
                return false;
            }
            List<PurchasableField> streetGroup = game.getGameMap().getGroup(streetGroupId);
            boolean allOwned = streetGroup.stream()
                    .noneMatch(PurchasableField::isFree);
            if (allOwned) {
                boolean sameOwner = streetGroup.stream()
                        .allMatch(f -> player.equals(f.getOwner()));
                if (sameOwner) {
                    return streetGroup.stream()
                            .filter(f -> !f.equals(streetField))
                            .map(f -> (StreetField) f)
                            .allMatch(f -> !f.isMortgaged() && f.getHouses() >= currentNumberOfHouses);
                }
            }
        }
        return false;
    }

    @Override
    public boolean houseSaleAvailable(Game game, StreetField streetField) {
        if (streetField.getHouses() > 0) {
            return game.getGameMap().getGroup(streetField.getGroupId()).stream()
                    .filter(f -> !f.equals(streetField))
                    .map(f -> (StreetField) f)
                    .allMatch(f -> f.getHouses() <= streetField.getHouses());
        }
        return false;
    }

    @Override
    public boolean mortgageAvailable(Game game, PurchasableField purchasableField) {
        if (!purchasableField.isMortgaged()) {
            if (purchasableField instanceof StreetField) {
                return game.getGameMap().getGroup(purchasableField.getGroupId()).stream()
                        .map(field -> (StreetField) field)
                        .allMatch(field -> field.getHouses() == 0);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean redemptionAvailable(Game game, PurchasableField purchasableField) {
        return purchasableField.isMortgaged()
                && game.getCurrentPlayer().getMoney() >= getPropertyRedemptionValue(purchasableField);
    }

    private int getPropertyRedemptionValue(PurchasableField purchasableField) {
        return purchasableField.getPrice() * 55 / 100;
    }

    private Pair<Integer, Integer> doFieldManagement(Game game, String playerId, int fieldIndex, BiConsumer<Game, PurchasableField> action) {
        checkFieldExists(fieldIndex);
        checkFieldManagementAvailability(game, playerId);

        Player currentPlayer = game.getCurrentPlayer();
        int beforeManagementMoneyAmount = currentPlayer.getMoney();
        GameField field = game.getGameMap().getField(fieldIndex);
        if (field instanceof PurchasableField && currentPlayer.equals(((PurchasableField) field).getOwner())) {
            var purchasableField = (PurchasableField) field;
            action.accept(game, purchasableField);
        } else {
            throw new IllegalStateException("Cannot manage field - it doesn't belong to the current player");
        }
        return Pair.of(beforeManagementMoneyAmount, currentPlayer.getMoney());
    }

    private void checkFieldManagementAvailability(Game game, String playerId) {
        GameStage stage = game.getStage();
        if (managementNotAvailable(stage)) {
            throw new IllegalStateException("Cannot perform field management - wrong game stage");
        }
        if (!game.getCurrentPlayer().getId().equals(playerId)) {
            throw new IllegalStateException("Only current player can manage fields");
        }
    }

    private boolean managementNotAvailable(GameStage stage) {
        return !GameStage.TURN_START.equals(stage)
                && !GameStage.JAIL_RELEASE_START.equals(stage)
                && !GameStage.AWAITING_PAYMENT.equals(stage)
                && !GameStage.AWAITING_JAIL_FINE.equals(stage)
                && !GameStage.BUY_PROPOSAL.equals(stage);
    }

    private void checkFieldExists(int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex > GameMap.LAST_FIELD_INDEX) {
            throw new IllegalArgumentException(String.format("Field with index %s don't exist", fieldIndex));
        }
    }

    private void notifyAfterHouseManagementChange(Player currentPlayer, StreetField streetField) {
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
        gameEventSender.sendToAllPlayers(new StreetHouseAmountEvent(streetField.getId(), streetField.getHouses()));
        gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(
                Collections.singletonList(gameFieldConverter.toView(streetField))));
    }
}
