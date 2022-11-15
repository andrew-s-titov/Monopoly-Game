package com.monopolynew.map;

import com.monopolynew.game.Rules;
import lombok.Getter;

import java.util.Objects;

public class StreetField extends BasePurchasableField {

    @Getter
    private final int housePrice;
    private final int[] rents;
    @Getter
    private int houses = 0;

    public StreetField(int id, String name, int group, int price, int housePrice, int[] rents) {
        super(id, name, group, price);
        this.housePrice = housePrice;
        if (rents.length != Rules.MAX_HOUSES_ON_STREET + 1) {
            throw new IllegalArgumentException("street field must contain exactly" + Rules.MAX_HOUSES_ON_STREET + 1 + "rent rates");
        }
        this.rents = rents;
    }

    public int computeRent(boolean allGroupOwnedByTheSamePlayer) {
        if (houses == 0) {
            return allGroupOwnedByTheSamePlayer ? rents[0] * 2 : rents[0];
        } else {
            return rents[houses];
        }
        // TODO: rewrite logic
    }

    public void pledge() {
        if (houses > 0) {
            throw new IllegalStateException("Cannot pledge field with houses");
        }
        this.mortgageTurns = Rules.MORTGAGE_TURNS;
    }

    public void addHouse() {
        if (houses == Rules.MAX_HOUSES_ON_STREET) {
            throw new IllegalStateException("Cannot add new house - limit is reached");
        }
        houses++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreetField that = (StreetField) o;
        return id == that.id && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}