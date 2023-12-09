package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.ActionableField;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.api.AuctionManager;
import com.monopolynew.service.api.GameLogicExecutor;
import com.monopolynew.service.api.PaymentProcessor;
import com.monopolynew.service.api.StepProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepProcessorImpl implements StepProcessor {

    private final GameLogicExecutor gameLogicExecutor;
    private final AuctionManager auctionManager;
    private final PaymentProcessor paymentProcessor;
    private final FieldActionExecutorDelegator fieldActionExecutor;

    @Override
    public void processStepOnField(Game game, GameField field) {
        if (field instanceof PurchasableField purchasableField) {
            processStepOnPurchasableField(game, purchasableField);
        } else if (field instanceof ActionableField actionableField) {
            fieldActionExecutor.execute(actionableField.getAction(), game);
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
                throw new IllegalStateException("Failed to compute rent - unknown field type");
            }
            initiateRentPayment(game, currentPlayer, field, rent);
        }
    }

    private boolean isRentPaymentNeeded(Game game, Player player, PurchasableField field) {
        if (field.isFree()) {
            int streetPrice = field.getPrice();
            if (streetPrice > player.getMoney()) {
                var playerAssets = gameLogicExecutor.computePlayerAssets(game, player);
                if (playerAssets >= streetPrice) {
                    gameLogicExecutor.sendBuyProposal(game, player, field, false);
                } else {
                    auctionManager.startNewAuction(game, field);
                }
            } else {
                gameLogicExecutor.sendBuyProposal(game, player, field, true);
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
        paymentProcessor.startPaymentProcess(game, player, owner, rent, paymentComment);
    }
}