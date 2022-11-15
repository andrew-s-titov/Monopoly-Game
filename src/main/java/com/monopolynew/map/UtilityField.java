package com.monopolynew.map;

import com.monopolynew.dto.DiceResult;

import java.util.Objects;

public class UtilityField extends BasePurchasableField {

    public UtilityField(int id, String name, int group, int price, int standardMultiplier, int highMultiplier) {
        super(id, name, group, price);
        this.standardMultiplier = standardMultiplier;
        this.highMultiplier = highMultiplier;
    }

    private final int standardMultiplier;
    private final int highMultiplier;

    public int computeFare(DiceResult diceResult, boolean allOwnedByTheSamePlayer) {
        int diceSum = diceResult.getSum();
        return allOwnedByTheSamePlayer ? diceSum * highMultiplier : diceSum * standardMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilityField that = (UtilityField) o;
        return id == that.id && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}