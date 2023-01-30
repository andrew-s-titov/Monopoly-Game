package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerDisconnectedEvent implements GameEvent {

    private final int code = 102;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("player_name")
    private final String playerName;
}