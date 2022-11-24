package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ArrestedFieldActionExecutor implements FieldActionExecutor {

    private final GameHelper gameHelper;

    @Getter
    private final FieldAction fieldAction = FieldAction.ARRESTED;

    @Override
    public void executeAction(Game game) {
        gameHelper.sendToJailAndEndTurn(game, game.getCurrentPlayer(), null);
    }
}
