package com.monopolynew.event;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public record ChatMessageEvent(@NonNull String message, @Nullable String playerId) {

    public ChatMessageEvent(@NonNull String message) {
        this(message, null);
    }

    private static final int code = 200;
}