package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerConnectedEvent implements GameEvent {

    private final int code = 101;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("player_name")
    private final String playerName;

    public static PlayerConnectedEvent fromPlayer(Player player) {
        return new PlayerConnectedEvent(player.getId(), player.getName());
    }
}