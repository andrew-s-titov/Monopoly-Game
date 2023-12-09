package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConnectionErrorEvent implements GameEvent {

    private final int code = 500;

    @JsonProperty("message")
    private final String message;
}
