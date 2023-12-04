package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.ChanceExecutor;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChanceFieldActionExecutor implements FieldActionExecutor {

    private final ChanceExecutor chanceExecutor;

    @Getter
    private final FieldAction fieldAction = FieldAction.CHANCE;

    @Override
    public void executeAction(Game game) {
        chanceExecutor.executeChance(game);
    }
}
