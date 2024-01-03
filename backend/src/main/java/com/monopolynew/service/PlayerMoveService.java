package com.monopolynew.service;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.chance.GoTo;
import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.map.ActionableField;
import com.monopolynew.map.ChanceField;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.fieldactionexecutors.ChanceExecutor;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutorDelegator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PlayerMoveService {

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;
    private final AuctionManager auctionManager;
    private final PaymentService paymentService;
    private final FieldActionExecutorDelegator fieldActionExecutor;
    private final ChanceExecutor chanceExecutor;
    private final ScheduledExecutorService scheduler;

    public void movePlayer(Game game, Player player, DiceResult diceResult) {
        movePlayerToPosition(game, player, computeNewPlayerPosition(player, diceResult), true);
    }

    private void movePlayerToPosition(Game game, Player player, int newPositionIndex, boolean forward) {
        int currentPosition = player.getPosition();
        gameLogicExecutor.changePlayerPosition(player, newPositionIndex);
        if (forward && newPositionIndex < currentPosition) {
            player.addMoney(Rules.CIRCLE_MONEY);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                    String.format("%s received $%s for starting a new circle",
                            player.getName(), Rules.CIRCLE_MONEY)));
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }

        GameField field = game.getGameMap().getField(newPositionIndex);
        scheduler.schedule(() -> processStepOnField(game, player, field),
                1000, TimeUnit.MILLISECONDS);
    }

    private void processStepOnField(Game game, Player player, GameField field) {
        if (field instanceof PurchasableField purchasableField) {
            processStepOnPurchasableField(game, purchasableField);
        } else if (field instanceof ActionableField actionableField) {
            fieldActionExecutor.execute(actionableField.getAction(), game);
        } else if (field instanceof ChanceField) {
            GoTo chanceResult = chanceExecutor.applyNextCard(game);
            if (chanceResult == null) {
                gameLogicExecutor.endTurn(game);
                return;
            }
            int whereTo = chanceResult.whereTo();
            if (whereTo == Rules.JAIL_FIELD_NUMBER) {
                gameLogicExecutor.sendToJail(game, player);
                gameLogicExecutor.endTurn(game);
            } else {
                movePlayerToPosition(game, player, whereTo, chanceResult.forward());
            }
        } else {
            // normally shouldn't happen
            throw new IllegalStateException("field on new player position is of an unsupported type");
        }
    }

    private void processStepOnPurchasableField(Game game, PurchasableField field) {
        Player currentPlayer = game.getCurrentPlayer();
        boolean rentPaymentNeeded = isRentPaymentNeeded(game, currentPlayer, field);
        if (rentPaymentNeeded) {
            int rent;
            if (field instanceof StaticRentField staticRentField) {
                rent = staticRentField.getCurrentRent();
            } else if (field instanceof UtilityField utilityField) {
                rent = utilityField.getRent(game.getLastDice());
            } else {
                // normally shouldn't happen
                throw new IllegalStateException("Failed to compute rent - unknown field type");
            }
            initiateRentPayment(game, currentPlayer, field, rent);
        }
    }

    private boolean isRentPaymentNeeded(Game game, Player player, PurchasableField field) {
        if (field.isFree()) {
            int streetPrice = field.getPrice();
            if (streetPrice > player.getMoney() &&
                    streetPrice > gameLogicExecutor.computePlayerAssets(game, player)) {
                auctionManager.startNewAuction(game, field);
            } else {
                gameLogicExecutor.sendBuyProposal(game, player, field);
            }
            return false;
        } else if (field.isMortgaged() || player.equals(field.getOwner())) {
            gameLogicExecutor.endTurn(game);
            return false;
        } else {
            return true;
        }
    }

    private void initiateRentPayment(Game game, Player player, PurchasableField field, int rent) {
        Player owner = field.getOwner();
        String paymentComment = String.format("%s is paying %s $%s rent for %s",
                player.getName(), owner.getName(), rent, field.getName());
        paymentService.startPaymentProcess(game, player, owner, rent, paymentComment);
    }

    public int computeNewPlayerPosition(Player player, DiceResult diceResult) {
        int result = player.getPosition() + diceResult.getSum();
        boolean newCircle = result > Rules.LAST_FIELD_INDEX;
        return newCircle ? result - Rules.NUMBER_OF_FIELDS : result;
    }
}
