package com.monopolynew.map;

import java.util.Objects;

public class CompanyField extends BasePurchasableField {

    private final int fare;

    public CompanyField(int id, String name, int group, int price, int fare) {
        super(id, name, group, price);
        this.fare = fare;
    }

    public int computeFare(int ownedByTheSamePlayer) {
        return (int) (fare * Math.pow(2, ownedByTheSamePlayer - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyField that = (CompanyField) o;
        return id == that.id && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}