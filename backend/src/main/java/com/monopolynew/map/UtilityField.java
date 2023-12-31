package com.monopolynew.map;

import com.monopolynew.game.Player;
import com.monopolynew.game.procedure.DiceResult;
import lombok.Getter;

import java.util.Objects;

import static com.monopolynew.game.Rules.INCREASED_UTILITY_MULTIPLIER;
import static com.monopolynew.game.Rules.STANDARD_UTILITY_MULTIPLIER;

@Getter
public class UtilityField extends BasePurchasableField {

    private int currentMultiplier;

    public UtilityField(int id, String name, int price) {
        super(id, name, price);
        this.currentMultiplier = STANDARD_UTILITY_MULTIPLIER;
    }

    @Override
    public void newOwner(Player newOwner) {
        super.newOwner(newOwner);
        if (newOwner == null) {
            this.currentMultiplier = STANDARD_UTILITY_MULTIPLIER;
        }
    }

    public void refreshRent(boolean allOwned) {
        this.currentMultiplier = allOwned ? INCREASED_UTILITY_MULTIPLIER : STANDARD_UTILITY_MULTIPLIER;
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