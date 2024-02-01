package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.service.PaymentService;

import java.util.Map;

public abstract class TaxFieldActionExecutor implements FieldActionExecutor {

    private final PaymentService paymentService;

    protected TaxFieldActionExecutor(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    protected void prepareTaxPayment(Game game, int tax, String translationKey) {
        Player currentPlayer = game.getCurrentPlayer();
        var systemMessage = new SystemMessageEvent(translationKey, Map.of(
                "name", currentPlayer.getName(),
                "amount", tax));
        paymentService.startPaymentProcess(game, currentPlayer, null, tax, systemMessage);
    }
}
