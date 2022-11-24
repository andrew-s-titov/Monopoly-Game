package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.PaymentProcessor;
import com.monopolynew.service.fieldactionexecutors.TaxFieldActionExecutor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncomeTaxFieldActionExecutor extends TaxFieldActionExecutor {

    @Autowired
    public IncomeTaxFieldActionExecutor(PaymentProcessor paymentProcessor) {
        super(paymentProcessor);
    }

    @Getter
    private final FieldAction fieldAction = FieldAction.INCOME_TAX;

    @Override
    public void executeAction(Game game) {
        prepareTaxPayment(game, Rules.INCOME_TAX, FieldAction.INCOME_TAX.getName());
    }
}