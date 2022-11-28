package com.monopolynew.service.fieldactionexecutors.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.fieldactionexecutors.FieldActionExecutor;
import com.monopolynew.websocket.GameEventSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class StartFieldActionExecutor implements FieldActionExecutor {

    private final GameEventSender gameEventSender;
    private final GameHelper gameHelper;

    @Getter
    private final FieldAction fieldAction = FieldAction.START;

    @Override
    public void executeAction(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.addMoney(Rules.CIRCLE_MONEY);
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s received $%s for hitting the %s field",
                        currentPlayer.getName(), Rules.CIRCLE_MONEY, FieldAction.START.getName())));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
        gameHelper.endTurn(game);
    }
}