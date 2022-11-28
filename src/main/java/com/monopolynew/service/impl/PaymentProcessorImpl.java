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
import com.monopolynew.service.PaymentProcessor;
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
    public void startPaymentProcess(Game game, Player player, Player beneficiary, int amount, String paymentComment) {
        var currentGameStage = game.getStage();
        verifyCheckCreationAvailable(currentGameStage);
        var newGameStage = GameStage.ROLLED_FOR_TURN.equals(currentGameStage) ?
                GameStage.AWAITING_PAYMENT : GameStage.AWAITING_JAIL_FINE;
        boolean payable = player.getMoney() >= amount;
        if (payable) {
            game.setStage(newGameStage);
            var checkToPay = new CheckToPay(player, beneficiary, amount, true, false, paymentComment);
            game.setCheckToPay(checkToPay);
            gameEventSender.sendToPlayer(player.getId(), PayCommandEvent.fromCheck(checkToPay));
        } else {
            int assets = gameHelper.computePlayerAssets(game, player);
            boolean enoughAssets = assets >= amount;
            if (enoughAssets) {
                game.setStage(newGameStage);
                var checkToPay = new CheckToPay(player, beneficiary, amount, false, assets * 0.9 < assets,
                        paymentComment);
                game.setCheckToPay(checkToPay);
                gameEventSender.sendToPlayer(player.getId(), PayCommandEvent.fromCheck(checkToPay));
            } else {
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(player.getName() + "went bankrupt"));
                gameHelper.bankruptPlayer(game, player);
            }
        }
    }

    @Override
    public void processPayment(Game game) {
        var currentGameStage = game.getStage();
        verifyPaymentProcessingAvailable(currentGameStage);
        var checkToPay = game.getCheckToPay();
        if (checkToPay == null) {
            throw new IllegalStateException("Cannot process payment - no payment found");
        }
        var payer = checkToPay.getPlayer();
        int sum = checkToPay.getSum();
        if (payer.getMoney() < sum) {
            throw new IllegalStateException("Player doesn't have enough money - payment is impossible");
        }
        payer.takeMoney(sum);
        List<MoneyState> moneyStates = new ArrayList<>(2);
        moneyStates.add(MoneyState.fromPlayer(payer));
        var beneficiary = checkToPay.getBeneficiary();
        if (beneficiary != null) {
            beneficiary.addMoney(sum);
            moneyStates.add(MoneyState.fromPlayer(beneficiary));
        }
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
        var paymentComment = checkToPay.getComment();
        if (StringUtils.isNotBlank(paymentComment)) {
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(paymentComment));
        }
        game.setCheckToPay(null);

        if (GameStage.AWAITING_PAYMENT.equals(currentGameStage)) {
            gameHelper.endTurn(game);
        } else if (GameStage.AWAITING_JAIL_FINE.equals(currentGameStage)) {
            payer.releaseFromJail();
            game.setStage(GameStage.ROLLED_FOR_TURN);
            var newPosition = gameHelper.computeNewPlayerPosition(payer, game.getLastDice());
            gameHelper.movePlayer(game, payer, newPosition, true);
        }
    }

    private void verifyCheckCreationAvailable(GameStage currentGameStage) {
        if (!GameStage.ROLLED_FOR_TURN.equals(currentGameStage) && !GameStage.ROLLED_FOR_JAIL.equals(currentGameStage)) {
            throw new IllegalStateException("Cannot create a check - wrong game stage");
        }
    }

    private void verifyPaymentProcessingAvailable(GameStage currentGameStage) {
        if (!GameStage.AWAITING_PAYMENT.equals(currentGameStage) && !GameStage.AWAITING_JAIL_FINE.equals(currentGameStage)) {
            throw new IllegalStateException("Cannot process payment - wrong game stage");
        }
    }
}
