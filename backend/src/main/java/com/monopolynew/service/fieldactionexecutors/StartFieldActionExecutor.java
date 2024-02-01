package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class StartFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.START;

    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    @Override
    public void executeAction(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.addMoney(Rules.CIRCLE_MONEY);
        var gameId = game.getId();
        gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.hitStart", Map.of(
                "name", currentPlayer.getName(),
                "amount", Rules.CIRCLE_MONEY)));
        gameEventSender.sendToAllPlayers(gameId, new MoneyChangeEvent(
                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
        gameLogicExecutor.endTurn(game);
    }
}