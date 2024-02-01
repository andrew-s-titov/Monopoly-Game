package com.monopolynew.service;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.procedure.CheckToPay;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;

    public void startPaymentProcess(Game game, @NonNull Player player, Player beneficiary,
                                    int amount, SystemMessageEvent paymentComment) {
        var gameId = game.getId();
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
            gameEventSender.sendToPlayer(gameId, player.getId(), PayCommandEvent.of(checkToPay));
        } else {
            var assets = gameLogicExecutor.computePlayerAssets(game, player);
            var checkToPay = CheckToPay.builder()
                    .debtor(player)
                    .beneficiary(beneficiary)
                    .debt(amount)
                    .wiseToGiveUp(assets * 0.85 < amount)
                    .comment(paymentComment)
                    .build();
            game.setCheckToPay(checkToPay);
            boolean enoughAssets = assets >= amount;
            if (enoughAssets) {
                gameLogicExecutor.changeGameStage(game, newGameStage);
                gameEventSender.sendToPlayer(gameId, player.getId(), PayCommandEvent.of(checkToPay));
            } else {
                gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.bankrupt", Map.of(
                        "name", player.getName())));
                gameLogicExecutor.bankruptPlayer(game, player);
            }
        }
    }

    public Player processPayment(Game game) {
        var gameId = game.getId();
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
        gameEventSender.sendToAllPlayers(gameId, new MoneyChangeEvent(moneyStates));
        var paymentCompleteSystemMessage = checkToPay.getComment();
        if (paymentCompleteSystemMessage != null) {
            gameEventSender.sendToAllPlayers(gameId, paymentCompleteSystemMessage);
        }
        game.setCheckToPay(null);
        return debtor;
    }

    private void verifyCheckCreationAvailable(GameStage currentGameStage) {
        if (!GameStage.ROLLED_FOR_TURN.equals(currentGameStage) && !GameStage.ROLLED_FOR_JAIL.equals(currentGameStage)) {
            throw new WrongGameStageException("Cannot create a check - wrong game stage");
        }
    }
}
