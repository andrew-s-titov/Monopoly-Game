package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ParkingFieldActionExecutor implements FieldActionExecutor {

    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    @Getter
    private final FieldAction fieldAction = FieldAction.PARKING;

    @Override
    public void executeAction(Game game) {
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                game.getCurrentPlayer().getName() + " is using free parking"));
        gameLogicExecutor.endTurn(game);
    }
}