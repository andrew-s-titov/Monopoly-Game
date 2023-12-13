package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;

@Getter
@Builder
public class ChatMessageEvent {

    private final int code = 200;
    private final String message;
    private final UUID playerId;

    @JsonCreator
    public ChatMessageEvent(@NonNull String message, @Nullable UUID playerId) {
        this.message = message;
        this.playerId = playerId;
    }

    public ChatMessageEvent(@NonNull String message) {
        this(message, null);
    }
}