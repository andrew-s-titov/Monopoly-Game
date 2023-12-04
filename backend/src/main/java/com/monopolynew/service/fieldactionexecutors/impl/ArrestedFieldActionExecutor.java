package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameLogicExecutor;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ArrestedFieldActionExecutor implements FieldActionExecutor {

    private final GameLogicExecutor gameLogicExecutor;

    @Getter
    private final FieldAction fieldAction = FieldAction.ARRESTED;

    @Override
    public void executeAction(Game game) {
        gameLogicExecutor.sendToJailAndEndTurn(game, game.getCurrentPlayer(), null);
    }
}
