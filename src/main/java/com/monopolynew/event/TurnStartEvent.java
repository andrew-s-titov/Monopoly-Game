package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TurnStartEvent implements WebsocketEvent {

    private final int code = 301;

    @JsonProperty("player_id")
    private final String playerId;

    public static TurnStartEvent forPlayer(Player player) {
        return new TurnStartEvent(player.getId());
    }
}
