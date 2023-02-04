package com.monopolynew.map;

import com.monopolynew.dto.DiceResult;
import lombok.Getter;

import java.util.Objects;

public class UtilityField extends BasePurchasableField {

    private final int standardMultiplier;
    private final int highMultiplier;

    @Getter
    private int currentMultiplier;

    public UtilityField(int id, String name, int price, int standardMultiplier, int highMultiplier) {
        super(id, name, price);
        this.standardMultiplier = standardMultiplier;
        this.highMultiplier = highMultiplier;
        this.currentMultiplier = standardMultiplier;
    }

    public void increaseMultiplier() {
        this.currentMultiplier = highMultiplier;
    }

    public void decreaseMultiplier() {
        this.currentMultiplier = standardMultiplier;
    }

    public int getRent(DiceResult diceResult) {
        return diceResult.getSum() * this.currentMultiplier;
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