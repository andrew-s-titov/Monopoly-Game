package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserIdentificationEvent implements WebsocketEvent {

    private final int code = 100;

    @JsonProperty("player_id")
    private final String playerId;
}