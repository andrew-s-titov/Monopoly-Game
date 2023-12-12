package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Builder
public class ChatMessageEvent {

    private final int code = 200;
    private final String message;
    private final String playerId;

    @JsonCreator
    public ChatMessageEvent(@NonNull String message, @Nullable String playerId) {
        this.message = message;
        this.playerId = playerId;
    }

    public ChatMessageEvent(@NonNull String message) {
        this(message, null);
    }
}