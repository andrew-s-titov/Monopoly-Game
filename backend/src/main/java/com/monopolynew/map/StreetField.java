package com.monopolynew.map;

import com.monopolynew.game.Rules;
import lombok.Getter;

import java.util.Objects;

public class StreetField extends BasePurchasableField implements StaticRentField {

    @Getter
    private final int housePrice;
    private final int[] rents;
    @Getter
    private int houses = 0;
    @Getter
    private int currentRent;

    public StreetField(int id, String name, int price, int housePrice, int[] rents) {
        super(id, name, price);
        this.housePrice = housePrice;
        if (rents.length != Rules.MAX_HOUSES_ON_STREET + 1) {
            throw new IllegalArgumentException("street field must contain exactly" + Rules.MAX_HOUSES_ON_STREET + 1 + "rent rates");
        }
        this.rents = rents;
    }

    public void setNewRent(boolean allGroupOwnedByTheSamePlayer) {
        if (houses == 0) {
            this.currentRent = allGroupOwnedByTheSamePlayer ? this.rents[0] * 2 : this.rents[0];
        } else {
            this.currentRent = this.rents[this.houses];
        }
    }

    @Override
    public void mortgage() {
        if (houses > 0) {
            throw new IllegalStateException("Cannot mortgage field with houses");
        }
        super.mortgage();
    }

    public void addHouse() {
        if (houses == Rules.MAX_HOUSES_ON_STREET) {
            throw new IllegalStateException("Cannot add new house - limit is reached");
        }
        this.houses++;
    }

    public void sellHouse() {
        if (houses == 0) {
            throw new IllegalStateException("Cannot add new house - limit is reached");
        }
        this.houses--;
    }

    public void sellAllHouses() {
        this.houses = 0;
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