package com.monopolynew.map;

import com.monopolynew.game.Player;
import lombok.Getter;

import java.util.Objects;

public class CompanyField extends BasePurchasableField implements StaticRentField {

    private final int defaultRent;

    @Getter
    private int currentRent;

    public CompanyField(int id, String name, int price, int defaultRent) {
        super(id, name, price);
        this.defaultRent = defaultRent;
    }
    @Override
    public void newOwner(Player newOwner) {
        super.newOwner(newOwner);
        if (newOwner == null) {
            this.currentRent = defaultRent;
        }
    }

    public void refreshRent(int ownedGroupCount) {
        this.currentRent = (int) (this.defaultRent * Math.pow(2, ownedGroupCount - 1));
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