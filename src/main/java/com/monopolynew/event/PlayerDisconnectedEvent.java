package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerDisconnectedEvent implements WebsocketEvent {

    private final int code = 102;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("player_name")
    private final String playerName;

    public static PlayerDisconnectedEvent fromPlayer(Player player) {
        return new PlayerDisconnectedEvent(player.getId(), player.getName());
    }
}