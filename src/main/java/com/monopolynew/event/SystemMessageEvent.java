package com.monopolynew.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SystemMessageEvent implements GameEvent {

    private final int code = 201;

    private final String message;
}