package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.ActionableField;
import com.monopolynew.map.FieldAction;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.PaymentProcessor;
import com.monopolynew.service.StepProcessor;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StepProcessorImpl implements StepProcessor {

    private final GameHelper gameHelper;
    private final AuctionManager auctionManager;
    private final PaymentProcessor paymentProcessor;
    private final Map<FieldAction, FieldActionExecutor> fieldActionExecutorMap;

    public StepProcessorImpl(GameHelper gameHelper, AuctionManager auctionManager,
                             PaymentProcessor paymentProcessor, List<FieldActionExecutor> fieldActionExecutors) {
        this.gameHelper = gameHelper;
        this.auctionManager = auctionManager;
        this.paymentProcessor = paymentProcessor;
        this.fieldActionExecutorMap = fieldActionExecutors.stream()
                .collect(Collectors.toMap(FieldActionExecutor::getFieldAction, e -> e));
    }

    @Override
    public void processStepOnField(Game game, GameField field) {
        if (field instanceof PurchasableField) {
            processStepOnPurchasableField(game, (PurchasableField) field);
        } else if (field instanceof ActionableField) {
            var actionableField = (ActionableField) field;
            fieldActionExecutorMap.get(actionableField.getAction()).executeAction(game);
        } else {
            throw new IllegalStateException("field on new player position is of an unsupported type");
        }
    }

    private void processStepOnPurchasableField(Game game, PurchasableField field) {
        Player currentPlayer = game.getCurrentPlayer();
        boolean rentPaymentNeeded = isRentPaymentNeeded(game, currentPlayer, field);
        if (rentPaymentNeeded) {
            int rent;
            if (field instanceof StaticRentField) {
                rent = ((StaticRentField) field).getCurrentRent();
            } else if (field instanceof UtilityField) {
                rent = ((UtilityField) field).getRent(game.getLastDice());
            } else {
                throw new IllegalStateException("Failed to compute rent - unknown field type");
            }
            initiateRentPayment(game, currentPlayer, field, rent);
        }
    }

    private boolean isRentPaymentNeeded(Game game, Player player, PurchasableField field) {
        if (field.isFree()) {
            int streetPrice = field.getPrice();
            if (streetPrice <= player.getMoney()) {
                gameHelper.sendBuyProposal(game, player, field);
            } else {
                // TODO: auto auction or let sell or mortgage something?
                auctionManager.startNewAuction(game, field);
            }
            return false;
        } else if (field.isMortgaged() || player.equals(field.getOwner())) {
            gameHelper.endTurn(game);
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