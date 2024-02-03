package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ChatMessageEvent {

    private final int code = 200;
    private final String message;
    private final UUID playerId;

    @JsonCreator
    public ChatMessageEvent(String message, UUID playerId) {
        this.message = message;
        this.playerId = playerId;
    }
}
