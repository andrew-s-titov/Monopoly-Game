package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameLogicExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TeleportFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.TELEPORT;

    private final GameLogicExecutor gameLogicExecutor;

    @Override
    public void executeAction(Game game) {
        // TODO: teleport implementation
        gameLogicExecutor.endTurn(game);
    }
}