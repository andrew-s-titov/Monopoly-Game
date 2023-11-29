package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChipMoveEvent implements GameEvent {

    private final int code = 304;

    private final String playerId;

    private final int field;

    private final boolean needAfterMoveCall;
}
