package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.api.PaymentProcessor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class IncomeTaxFieldActionExecutor extends TaxFieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.INCOME_TAX;

    public IncomeTaxFieldActionExecutor(PaymentProcessor paymentProcessor) {
        super(paymentProcessor);
    }

    @Override
    public void executeAction(Game game) {
        prepareTaxPayment(game, Rules.INCOME_TAX, FieldAction.INCOME_TAX.getName());
    }
}