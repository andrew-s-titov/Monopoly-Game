package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DiceResultEvent implements GameEvent {

    private final int code = 303;

    private final String playerId;

    private final int firstDice;

    private final int secondDice;
}
