package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        gameEventSender.sendToAllPlayers(game.getId(), new SystemMessageEvent("event.jail.visit", Map.of(
                "name", currentPlayer.getName())));
        gameLogicExecutor.endTurn(game);
    }
}
