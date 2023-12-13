package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DiceResultEvent implements GameEvent {

    private final int code = 303;

    private final UUID playerId;

    private final int firstDice;

    private final int secondDice;
}
