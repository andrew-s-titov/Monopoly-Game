package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.MoneyChangeEvent;
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
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                String.format("%s received $%s for hitting the %s field",
                        currentPlayer.getName(), Rules.CIRCLE_MONEY, FieldAction.START.getName())));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
        gameLogicExecutor.endTurn(game);
    }
}