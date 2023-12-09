package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.api.ChanceExecutor;
import com.monopolynew.service.fieldactionexecutors.api.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChanceFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private static final FieldAction fieldAction = FieldAction.CHANCE;

    private final ChanceExecutor chanceExecutor;

    @Override
    public void executeAction(Game game) {
        chanceExecutor.executeChance(game);
    }
}
