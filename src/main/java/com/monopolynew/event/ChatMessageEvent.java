package com.monopolynew.event;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class ChatMessageEvent {

    private final int code = 200;
    private final String message;
    private final String playerId;

    public ChatMessageEvent(@NonNull String message, @Nullable String playerId) {
        this.message = message;
        this.playerId = playerId;
    }

    public ChatMessageEvent(@NonNull String message) {
        this(message, null);
    }
}