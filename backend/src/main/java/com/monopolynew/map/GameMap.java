package com.monopolynew.map;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.monopolynew.game.Rules.LAST_FIELD_INDEX;

public class GameMap {

    // TODO: send this data to FE
    // TODO: instead of int use some text marker like color on FE
    private final List<Integer> housePurchaseMade;
    @Getter
    private final List<GameField> fields;

    public GameMap(boolean withTeleport) {
        this.fields = createFields(withTeleport);
        this.housePurchaseMade = new ArrayList<>(10);
    }

    public GameField getField(int position) {
        if (position > LAST_FIELD_INDEX || position < 0) {
            throw new IllegalArgumentException("no field on position " + position);
        }
        return fields.get(position);
    }

    public boolean isPurchaseMadeForGroup(int groupId) {
        return this.housePurchaseMade.contains(groupId);
    }

    public void setPurchaseMadeFlag(int groupId) {
        this.housePurchaseMade.add(groupId);
    }

    public void resetPurchaseHistory() {
        this.housePurchaseMade.clear();
    }

    private List<GameField> createFields(boolean withTeleport) {
        return List.of(
                new ActionableField(0, FieldAction.START),
                new StreetField(1, "Saint Petersburg", 60, 50, new int[]{2, 10, 30, 90, 160, 250}),
                new ActionableField(2, FieldAction.CHANCE),
                new StreetField(3, "Moscow", 60, 50, new int[]{4, 20, 60, 180, 320, 450}),
                new ActionableField(4, FieldAction.INCOME_TAX),
                new CompanyField(5, "Ryanair", 200, 25),
                new StreetField(6, "Helsinki", 100, 50, new int[]{6, 30, 90, 270, 400, 550}),
                new ActionableField(7, FieldAction.CHANCE),
                new StreetField(8, "Oslo", 100, 50, new int[]{6, 30, 90, 270, 400, 550}),
                new StreetField(9, "Stockholm", 120, 50, new int[]{8, 40, 100, 300, 450, 600}),
                new ActionableField(10, FieldAction.JAIL),
                new StreetField(11, "Budapest", 140, 100, new int[]{10, 50, 150, 450, 625, 750}),
                new UtilityField(12, "Electric Company", 150),
                new StreetField(13, "Prague", 140, 100, new int[]{10, 50, 150, 450, 625, 750}),
                new StreetField(14, "Vienna", 160, 100, new int[]{12, 60, 180, 500, 700, 900}),
                new CompanyField(15, "KLM", 200, 25),
                new StreetField(16, "Venice", 180, 100, new int[]{14, 70, 200, 550, 750, 950}),
                new ActionableField(17, FieldAction.CHANCE),
                new StreetField(18, "Milan", 180, 100, new int[]{14, 70, 200, 550, 750, 950}),
                new StreetField(19, "Rome", 200, 100, new int[]{16, 80, 220, 600, 800, 1000}),
                withTeleport ? new ActionableField(20, FieldAction.TELEPORT) : new ActionableField(20, FieldAction.PARKING),
                new StreetField(21, "Lisbon", 220, 150, new int[]{18, 90, 250, 700, 875, 1050}),
                new ActionableField(22, FieldAction.CHANCE),
                new StreetField(23, "Madrid", 220, 150, new int[]{18, 90, 250, 700, 875, 1050}),
                new StreetField(24, "Athens", 240, 150, new int[]{20, 100, 300, 750, 925, 1100}),
                new CompanyField(25, "Lufthansa", 200, 25),
                new StreetField(26, "Geneva", 260, 150, new int[]{22, 110, 330, 800, 975, 1150}),
                new StreetField(27, "Hamburg", 260, 150, new int[]{22, 110, 330, 800, 975, 1150}),
                new UtilityField(28, "Petrol Company", 150),
                new StreetField(29, "Berlin", 280, 150, new int[]{24, 120, 360, 850, 1025, 1200}),
                new ActionableField(30, FieldAction.ARRESTED),
                new StreetField(31, "Luxembourg", 300, 200, new int[]{26, 130, 390, 900, 1100, 1275}),
                new StreetField(32, "Brussels", 300, 200, new int[]{26, 130, 390, 900, 1100, 1275}),
                new ActionableField(33, FieldAction.CHANCE),
                new StreetField(34, "Amsterdam", 320, 200, new int[]{28, 150, 450, 1000, 1200, 1400}),
                new CompanyField(35, "British Airways", 200, 25),
                new ActionableField(36, FieldAction.LUXURY_TAX),
                new StreetField(37, "Paris", 350, 200, new int[]{35, 175, 500, 1100, 1300, 1500}),
                new ActionableField(38, FieldAction.CHANCE),
                new StreetField(39, "London", 400, 200, new int[]{50, 200, 600, 1400, 1700, 2000})
        );
    }
}