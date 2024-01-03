package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.service.PaymentService;

public abstract class TaxFieldActionExecutor implements FieldActionExecutor {

    private final PaymentService paymentService;

    protected TaxFieldActionExecutor(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    protected void prepareTaxPayment(Game game, int tax, String taxName) {
        Player currentPlayer = game.getCurrentPlayer();
        String paymentComment = String.format("%s is paying $%s as %s", currentPlayer.getName(), tax, taxName);
        paymentService.startPaymentProcess(game, currentPlayer, null, tax, paymentComment);
    }
}
