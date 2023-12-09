package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.service.api.GameLogicExecutor;
import com.monopolynew.service.api.FieldActionExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ParkingFieldActionExecutor implements FieldActionExecutor {

    @Getter
    private final FieldAction fieldAction = FieldAction.PARKING;

    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    @Override
    public void executeAction(Game game) {
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                game.getCurrentPlayer().getName() + " is using free parking"));
        gameLogicExecutor.endTurn(game);
    }
}