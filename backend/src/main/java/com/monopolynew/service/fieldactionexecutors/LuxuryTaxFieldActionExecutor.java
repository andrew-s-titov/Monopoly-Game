package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.PaymentService;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class LuxuryTaxFieldActionExecutor extends TaxFieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.LUXURY_TAX;

    public LuxuryTaxFieldActionExecutor(PaymentService paymentService) {
        super(paymentService);
    }

    @Override
    public void executeAction(Game game) {
        prepareTaxPayment(game, Rules.LUXURY_TAX, FieldAction.LUXURY_TAX.getName());
    }
}