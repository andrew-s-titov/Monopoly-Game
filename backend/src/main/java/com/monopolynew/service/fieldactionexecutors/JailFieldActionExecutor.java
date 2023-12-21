package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JailFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.JAIL;

    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    @Override
    public void executeAction(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                currentPlayer.getName() + " is visiting Jail for a tour"));
        gameLogicExecutor.endTurn(game);
    }
}