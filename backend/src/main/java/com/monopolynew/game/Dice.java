package com.monopolynew.game;

import com.monopolynew.dto.DiceResult;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Dice {

    public DiceResult rollTheDice() {
        var random = new Random();
        return new DiceResult(diceResult(random), diceResult(random));
    }

    private int diceResult(Random random) {
        return random.nextInt(6) + 1;
    }
}