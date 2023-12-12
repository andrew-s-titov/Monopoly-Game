package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DiceRollingStartEvent implements GameEvent {

    private final int code = 302;

    private final UUID playerId;
}