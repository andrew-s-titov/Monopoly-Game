package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TurnStartEvent implements GameEvent {

    private final int code = 301;

    @JsonProperty("player_id")
    private final String playerId;
}
