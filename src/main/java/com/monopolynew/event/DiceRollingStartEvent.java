package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiceRollingStartEvent implements WebsocketEvent {

    private final int code = 302;

    private final String playerId;

    public static DiceRollingStartEvent forPlayer(Player player) {
        return new DiceRollingStartEvent(player.getId());
    }
}
