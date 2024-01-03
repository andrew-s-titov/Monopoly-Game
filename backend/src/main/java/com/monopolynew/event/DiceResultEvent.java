package com.monopolynew.event;

import com.monopolynew.game.procedure.DiceResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DiceResultEvent implements GameEvent {

    private final int code = 303;

    private final int firstDice;

    private final int secondDice;

    public static DiceResultEvent of(DiceResult diceResult) {
        return new DiceResultEvent(diceResult.getFirstDice(), diceResult.getSecondDice());
    }
}
