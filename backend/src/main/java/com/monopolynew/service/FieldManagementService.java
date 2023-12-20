package com.monopolynew.service;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.mapper.GameFieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Component
public class FieldManagementService {

    private final GameEventSender gameEventSender;
    private final GameFieldMapper gameFieldMapper;
    private final GameLogicExecutor gameLogicExecutor;

    public void mortgageField(Game game, int fieldIndex, UUID playerId) {
        doFieldManagement(game, playerId, fieldIndex, (g, f) -> {
            if (mortgageAvailable(g, f)) {
                Player currentPlayer = g.getCurrentPlayer();
                f.mortgage();
                currentPlayer.addMoney(gameLogicExecutor.getFieldMortgagePrice(f));
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(
                        Collections.singletonList(gameFieldMapper.toState(f))));
                return;
            }
            throw new ClientBadRequestException("Cannot mortgage street with houses");
        });
    }

    public void redeemMortgagedProperty(Game game, int fieldIndex, UUID playerId) {
        doFieldManagement(game, playerId, fieldIndex, (g, f) -> {
            if (redemptionAvailable(g, f)) {
                Player currentPlayer = g.getCurrentPlayer();
                f.redeem();
                currentPlayer.takeMoney(getPropertyRedemptionValue(f));
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(
                        Collections.singletonList(gameFieldMapper.toState(f))));
                return;
            }
            throw new ClientBadRequestException("Cannot redeem property - not enough money");
        });
    }

    public void buyHouse(Game game, int fieldIndex, UUID playerId) {
        doFieldManagement(game, playerId, fieldIndex, (aGame, field) -> {
            if (field instanceof StreetField streetField) {
                Player currentPlayer = aGame.getCurrentPlayer();
                if (housePurchaseAvailable(aGame, currentPlayer, streetField)) {
                    streetField.addHouse();
                    aGame.getGameMap().setPurchaseMadeFlag(PurchasableFieldGroups.getGroupIdByFieldIndex(fieldIndex));
                    currentPlayer.takeMoney(streetField.getHousePrice());
                    notifyAfterHouseManagementChange(currentPlayer, streetField);
                    return;
                }
            }
            throw new ClientBadRequestException("Cannot buy a house for this property field");
        });
    }

    public void sellHouse(Game game, int fieldIndex, UUID playerId) {
        doFieldManagement(game, playerId, fieldIndex, (aGame, field) -> {
            if (field instanceof StreetField streetField && houseSaleAvailable(aGame, streetField)) {
                streetField.sellHouse();
                Player currentPlayer = aGame.getCurrentPlayer();
                currentPlayer.addMoney(streetField.getHousePrice());
                notifyAfterHouseManagementChange(currentPlayer, streetField);
                return;
            }
            throw new ClientBadRequestException("Cannot sell a house on this property field");
        });
    }

    public boolean housePurchaseAvailable(Game game, Player player, StreetField streetField) {
        if (streetField.isMortgaged()) {
            return false;
        }
        int currentNumberOfHouses = streetField.getHouses();
        if (currentNumberOfHouses < Rules.MAX_HOUSES_ON_STREET && player.getMoney() >= streetField.getHousePrice()) {
            int streetGroupId = PurchasableFieldGroups.getGroupIdByFieldIndex(streetField.getId());
            if (game.getGameMap().isPurchaseMadeForGroup(streetGroupId)) {
                return false;
            }
            List<PurchasableField> streetGroup = PurchasableFieldGroups.getGroupById(game, streetGroupId);
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

    public boolean houseSaleAvailable(Game game, StreetField streetField) {
        if (streetField.getHouses() > 0) {
            return PurchasableFieldGroups.getGroupByFieldIndex(game, streetField.getId()).stream()
                    .filter(f -> !f.equals(streetField))
                    .map(f -> (StreetField) f)
                    .allMatch(f -> f.getHouses() <= streetField.getHouses());
        }
        return false;
    }

    public boolean mortgageAvailable(Game game, PurchasableField purchasableField) {
        if (!purchasableField.isMortgaged()) {
            if (purchasableField instanceof StreetField) {
                return PurchasableFieldGroups.getGroupByFieldIndex(game, purchasableField.getId()).stream()
                        .map(StreetField.class::cast)
                        .allMatch(field -> field.getHouses() == 0);
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean redemptionAvailable(Game game, PurchasableField purchasableField) {
        return purchasableField.isMortgaged()
                && game.getCurrentPlayer().getMoney() >= getPropertyRedemptionValue(purchasableField);
    }

    private int getPropertyRedemptionValue(PurchasableField purchasableField) {
        return purchasableField.getPrice() * 55 / 100;
    }

    private void doFieldManagement(Game game, UUID playerId, int fieldIndex, BiConsumer<Game, PurchasableField> action) {
        checkFieldExists(fieldIndex);
        checkFieldManagementAvailability(game, playerId);

        Player currentPlayer = game.getCurrentPlayer();
        GameField field = game.getGameMap().getField(fieldIndex);
        if (field instanceof PurchasableField purchasableField
                && currentPlayer.equals(((PurchasableField) field).getOwner())) {
            action.accept(game, purchasableField);
        } else {
            throw new ClientBadRequestException("Cannot manage field - it doesn't belong to the current player");
        }
    }

    private void checkFieldManagementAvailability(Game game, UUID playerId) {
        GameStage stage = game.getStage();
        if (managementNotAvailable(stage)) {
            throw new WrongGameStageException("Cannot perform field management - wrong game stage");
        }
        if (!game.getCurrentPlayer().getId().equals(playerId)) {
            throw new ClientBadRequestException("Only current player can manage fields");
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
        if (fieldIndex < 0 || fieldIndex > Rules.LAST_FIELD_INDEX) {
            throw new ClientBadRequestException(String.format("Field with index %s don't exist", fieldIndex));
        }
    }

    private void notifyAfterHouseManagementChange(Player currentPlayer, StreetField streetField) {
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
        gameEventSender.sendToAllPlayers(new FieldStateChangeEvent(
                Collections.singletonList(gameFieldMapper.toState(streetField))));
    }
}
