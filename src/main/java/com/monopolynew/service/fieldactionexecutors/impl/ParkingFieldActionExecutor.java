package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import com.monopolynew.websocket.GameEventSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ParkingFieldActionExecutor implements FieldActionExecutor {

    private final GameEventSender gameEventSender;
    private final GameHelper gameHelper;

    @Getter
    private final FieldAction fieldAction = FieldAction.PARKING;

    @Override
    public void executeAction(Game game) {
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                game.getCurrentPlayer() + " is using free parking"));
        gameHelper.endTurn(game);
    }
}