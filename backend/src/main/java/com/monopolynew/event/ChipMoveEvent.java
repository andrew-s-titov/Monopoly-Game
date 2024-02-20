package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ChipMoveEvent implements GameEvent {

    private final int code = 304;

    private final UUID playerId;

    private final int field;

    private final boolean forward;
}
