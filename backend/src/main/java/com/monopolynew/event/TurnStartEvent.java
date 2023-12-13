package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class TurnStartEvent implements GameEvent {

    private final int code = 301;

    private final UUID playerId;
}
