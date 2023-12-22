package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.chance.ChanceCardProvider;
import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChanceFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.CHANCE;

    private final ChanceCardProvider chanceCardProvider;

    @Override
    public void executeAction(Game game) {
        chanceCardProvider.applyNextCard(game);
    }
}
