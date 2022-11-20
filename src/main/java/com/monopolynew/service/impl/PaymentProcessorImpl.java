package com.monopolynew.service.impl;

import com.monopolynew.dto.CheckToPay;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentProcessorImpl implements PaymentProcessor {

    private final GameHelper gameHelper;
    private final GameEventSender gameEventSender;

    @Override
    public void createPayCheck(Game game, Player player, Player beneficiary, int amount, String paymentComment) {
        boolean payable = player.getMoney() >= amount;
        if (payable) {
            game.setStage(GameStage.AWAITING_PAYMENT);
            CheckToPay checkToPay = new CheckToPay(player, beneficiary, amount, true, false, paymentComment);
            game.setCheckToPay(checkToPay);
            gameEventSender.sendToPlayer(player.getId(), PayCommandEvent.fromCheck(checkToPay));
        } else {
            int assets = gameHelper.computePlayerAssets(game, player);
            boolean enoughAssets = assets >= amount;
            if (enoughAssets) {
                game.setStage(GameStage.AWAITING_PAYMENT);
                CheckToPay checkToPay = new CheckToPay(player, beneficiary, amount, false, amount >= assets * 0.9,
                        paymentComment);
                game.setCheckToPay(checkToPay);
                gameEventSender.sendToPlayer(player.getId(), PayCommandEvent.fromCheck(checkToPay));
            } else {
                // TODO: auto-bankruptcy
                gameHelper.endTurn(game);
            }
        }
    }


    @Override
    public void processPayment(Game game) {
        if (!GameStage.AWAITING_PAYMENT.equals(game.getStage())) {
            throw new IllegalStateException("Cannot process payment - wrong game stage");
        }
        CheckToPay checkToPay = game.getCheckToPay();
        if (checkToPay == null) {
            throw new IllegalStateException("Cannot process payment - no payment found");
        }
        Player payer = checkToPay.getPlayer();
        int sum = checkToPay.getSum();
        if (payer.getMoney() < sum) {
            throw new IllegalStateException("Player doesn't have enough money - payment is impossible");
        }
        payer.takeMoney(sum);
        List<MoneyState> moneyStates = new ArrayList<>(2);
        moneyStates.add(MoneyState.fromPlayer(payer));
        Player beneficiary = checkToPay.getBeneficiary();
        if (beneficiary != null) {
            beneficiary.addMoney(sum);
            moneyStates.add(MoneyState.fromPlayer(beneficiary));
        }
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
        String paymentComment = checkToPay.getComment();
        if (StringUtils.isNotBlank(paymentComment)) {
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(paymentComment));
        }
        game.setCheckToPay(null);
        game.setStage(GameStage.TURN_START);
        gameHelper.endTurn(game);
    }
}