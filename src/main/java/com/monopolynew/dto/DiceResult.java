package com.monopolynew.dto;

import lombok.Getter;

@Getter
public class DiceResult {
    private final int firstDice;
    private final int secondDice;
    private final boolean doublet;

    public DiceResult(int firstDice, int secondDice) {
        this.firstDice = firstDice;
        this.secondDice = secondDice;
        this.doublet = firstDice == secondDice;
    }

    public int getSum() {
        return firstDice + secondDice;
    }
}