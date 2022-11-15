package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatMessageEvent implements WebsocketEvent {

    private final int code = 200;

    @JsonProperty("player_id")
    private final String playerId;

    private final String message;
}