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
public class LuxuryTaxFieldActionExecutor extends TaxFieldActionExecutor {

    @Autowired
    public LuxuryTaxFieldActionExecutor(PaymentProcessor paymentProcessor) {
        super(paymentProcessor);
    }

    @Getter
    private final FieldAction fieldAction = FieldAction.LUXURY_TAX;

    @Override
    public void executeAction(Game game) {
        prepareTaxPayment(game, Rules.LUXURY_TAX, FieldAction.LUXURY_TAX.getName());
    }
}