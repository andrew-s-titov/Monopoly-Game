package com.monopolynew.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SystemMessageEvent implements WebsocketEvent {

    private final int code = 201;

    private final String message;

    public static SystemMessageEvent text(String message) {
        return new SystemMessageEvent(message);
    }
}