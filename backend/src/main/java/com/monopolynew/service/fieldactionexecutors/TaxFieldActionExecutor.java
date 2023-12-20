package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.service.PaymentProcessor;

public abstract class TaxFieldActionExecutor implements FieldActionExecutor {

    private final PaymentProcessor paymentProcessor;

    protected TaxFieldActionExecutor(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    protected void prepareTaxPayment(Game game, int tax, String taxName) {
        Player currentPlayer = game.getCurrentPlayer();
        String paymentComment = String.format("%s is paying $%s as %s", currentPlayer.getName(), tax, taxName);
        paymentProcessor.startPaymentProcess(game, currentPlayer, null, tax, paymentComment);
    }
}
