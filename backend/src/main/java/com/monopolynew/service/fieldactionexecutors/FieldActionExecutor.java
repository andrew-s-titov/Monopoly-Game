package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;

public interface FieldActionExecutor {

    FieldAction getFieldAction();

    void executeAction(Game game);
}
