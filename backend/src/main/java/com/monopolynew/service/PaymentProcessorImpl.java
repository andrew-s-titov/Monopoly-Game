package com.monopolynew.service;

import com.monopolynew.game.procedure.CheckToPay;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.service.api.GameEventGenerator;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.service.api.GameLogicExecutor;
import com.monopolynew.service.api.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentProcessorImpl implements PaymentProcessor {

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;

    @Override
    public void startPaymentProcess(Game game, @NonNull Player player, Player beneficiary,
                                    int amount, String paymentComment) {
        var currentGameStage = game.getStage();
        verifyCheckCreationAvailable(currentGameStage);
        var newGameStage = GameStage.ROLLED_FOR_TURN.equals(currentGameStage) ?
                GameStage.AWAITING_PAYMENT : GameStage.AWAITING_JAIL_FINE;
        boolean payable = player.getMoney() >= amount;
        if (payable) {
            gameLogicExecutor.changeGameStage(game, newGameStage);
            var checkToPay = CheckToPay.builder()
                    .debtor(player)
                    .beneficiary(beneficiary)
                    .debt(amount)
                    .wiseToGiveUp(false)
                    .comment(paymentComment)
                    .build();
            game.setCheckToPay(checkToPay);
            gameEventSender.sendToPlayer(player.getId(), gameEventGenerator.payCommandEvent(checkToPay));
        } else {
            var assets = gameLogicExecutor.computePlayerAssets(game, player);
            boolean enoughAssets = assets >= amount;
            if (enoughAssets) {
                gameLogicExecutor.changeGameStage(game, newGameStage);
                var checkToPay = CheckToPay.builder()
                        .debtor(player)
                        .beneficiary(beneficiary)
                        .debt(amount)
                        .wiseToGiveUp(assets * 0.85 < amount)
                        .comment(paymentComment)
                        .build();
                game.setCheckToPay(checkToPay);
                gameEventSender.sendToPlayer(player.getId(), gameEventGenerator.payCommandEvent(checkToPay));
            } else {
                gameEventSender.sendToAllPlayers(new ChatMessageEvent(player.getName() + " went bankrupt"));
                gameLogicExecutor.bankruptPlayer(game, player);
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
        var debtor = checkToPay.getDebtor();
        int debt = checkToPay.getDebt();
        if (debtor.getMoney() < debt) {
            throw new ClientBadRequestException("Player doesn't have enough money - payment is impossible");
        }
        debtor.takeMoney(debt);
        List<MoneyState> moneyStates = new ArrayList<>(2);
        moneyStates.add(MoneyState.fromPlayer(debtor));
        var beneficiary = checkToPay.getBeneficiary();
        if (beneficiary != null) {
            beneficiary.addMoney(debt);
            moneyStates.add(MoneyState.fromPlayer(beneficiary));
        }
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
        var paymentComment = checkToPay.getComment();
        if (StringUtils.isNotBlank(paymentComment)) {
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(paymentComment));
        }
        game.setCheckToPay(null);

        if (GameStage.AWAITING_PAYMENT.equals(currentGameStage)) {
            gameLogicExecutor.endTurn(game);
        } else if (GameStage.AWAITING_JAIL_FINE.equals(currentGameStage)) {
            debtor.releaseFromJail();
            gameLogicExecutor.changeGameStage(game, GameStage.ROLLED_FOR_TURN);
            var newPosition = gameLogicExecutor.computeNewPlayerPosition(debtor, game.getLastDice());
            gameLogicExecutor.movePlayer(game, debtor, newPosition, true);
        }
    }

    private void verifyCheckCreationAvailable(GameStage currentGameStage) {
        if (!GameStage.ROLLED_FOR_TURN.equals(currentGameStage) && !GameStage.ROLLED_FOR_JAIL.equals(currentGameStage)) {
            throw new WrongGameStageException("Cannot create a check - wrong game stage");
        }
    }

    private void verifyPaymentProcessingAvailable(GameStage currentGameStage) {
        if (!GameStage.AWAITING_PAYMENT.equals(currentGameStage) && !GameStage.AWAITING_JAIL_FINE.equals(currentGameStage)) {
            throw new WrongGameStageException("Cannot process payment - wrong game stage");
        }
    }
}
