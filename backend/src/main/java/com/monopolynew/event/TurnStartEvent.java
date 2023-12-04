package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TurnStartEvent implements GameEvent {

    private final int code = 301;

    private final String playerId;
}
