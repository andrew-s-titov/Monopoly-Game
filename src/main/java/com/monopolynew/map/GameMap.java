package com.monopolynew.map;

import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap {

    public static final int NUMBER_OF_FIELDS = 40;
    public static final int LAST_FIELD_INDEX = NUMBER_OF_FIELDS - 1;
    public static final int COMPANY_FIELD_GROUP = 1;
    public static final int UTILITY_FIELD_GROUP = 4;
    public static final int JAIL_FIELD_NUMBER = 10;

    private final Map<Integer, List<PurchasableField>> groups;
    private final Map<Integer, Boolean> housePurchaseMade;
    @Getter
    private final List<GameField> fields;

    public GameMap(boolean withTeleport) {
        this.fields = createFields(withTeleport);
        this.groups = defineGroupsMap(fields);
        this.housePurchaseMade = createHousePurchaseMadeMap(groups);
    }

    public GameField getField(int position) {
        if (position > LAST_FIELD_INDEX || position < 0) {
            throw new IllegalArgumentException("no field on position " + position);
        }
        return fields.get(position);
    }

    public List<PurchasableField> getGroup(int groupId) {
        List<PurchasableField> fieldGroup = this.groups.get(groupId);
        if (CollectionUtils.isEmpty(fieldGroup)) {
            throw new IllegalArgumentException("no group found for id " + groupId);
        }
        return fieldGroup;
    }

    public boolean isPurchaseMadeForGroup(int groupId) {
        if (!housePurchaseMade.containsKey(groupId)) {
            throw new IllegalArgumentException("no group found for id " + groupId);
        }
        return this.housePurchaseMade.get(groupId);
    }

    public void setPurchaseMadeFlag(int groupId) {
        if (!housePurchaseMade.containsKey(groupId)) {
            throw new IllegalArgumentException("no group found for id " + groupId);
        }
        this.housePurchaseMade.put(groupId, true);
    }

    public void resetPurchaseHistory() {
        this.housePurchaseMade.forEach((group, purchased) -> {
            if (purchased) housePurchaseMade.put(group, false);
        });
    }

    private List<GameField> createFields(boolean withTeleport) {
        return List.of(
                new ActionableField(0, FieldAction.START),
                new StreetField(1, "Saint Petersburg", 0, 60, 50, new int[]{2, 10, 30, 90, 160, 250}),
                new ActionableField(2, FieldAction.CHANCE),
                new StreetField(3, "Moscow", 0, 60, 50, new int[]{4, 20, 60, 180, 320, 450}),
                new ActionableField(4, FieldAction.INCOME_TAX),
                new CompanyField(5, "Ryanair", COMPANY_FIELD_GROUP, 200, 25),
                new StreetField(6, "Helsinki", 2, 100, 50, new int[]{6, 30, 90, 270, 400, 550}),
                new ActionableField(7, FieldAction.CHANCE),
                new StreetField(8, "Oslo", 2, 100, 50, new int[]{6, 30, 90, 270, 400, 550}),
                new StreetField(9, "Stockholm", 2, 120, 50, new int[]{8, 40, 100, 300, 450, 600}),
                new ActionableField(10, FieldAction.JAIL),
                new StreetField(11, "Budapest", 3, 140, 100, new int[]{10, 50, 150, 450, 625, 750}),
                new UtilityField(12, "Electric Company", UTILITY_FIELD_GROUP, 150, 4, 10),
                new StreetField(13, "Prague", 3, 140, 100, new int[]{10, 50, 150, 450, 625, 750}),
                new StreetField(14, "Vienna", 3, 160, 100, new int[]{12, 60, 180, 500, 700, 900}),
                new CompanyField(15, "KLM", COMPANY_FIELD_GROUP, 200, 25),
                new StreetField(16, "Venice", 5, 180, 100, new int[]{14, 70, 200, 550, 750, 950}),
                new ActionableField(17, FieldAction.CHANCE),
                new StreetField(18, "Milan", 5, 180, 100, new int[]{14, 70, 200, 550, 750, 950}),
                new StreetField(19, "Rome", 5, 200, 100, new int[]{16, 80, 220, 600, 800, 1000}),
                withTeleport ? new ActionableField(20, FieldAction.TELEPORT) : new ActionableField(20, FieldAction.PARKING),
                new StreetField(21, "Lisbon", 6, 220, 150, new int[]{18, 90, 250, 700, 875, 1050}),
                new ActionableField(22, FieldAction.CHANCE),
                new StreetField(23, "Madrid", 6, 220, 150, new int[]{18, 90, 250, 700, 875, 1050}),
                new StreetField(24, "Athens", 6, 240, 150, new int[]{20, 100, 300, 750, 925, 1000}),
                new CompanyField(25, "Lufthansa", COMPANY_FIELD_GROUP, 200, 25),
                new StreetField(26, "Geneva", 7, 260, 150, new int[]{22, 110, 330, 800, 975, 1150}),
                new StreetField(27, "Hamburg", 7, 260, 150, new int[]{22, 110, 330, 800, 975, 1150}),
                new UtilityField(28, "Petrol Company", UTILITY_FIELD_GROUP, 150, 4, 10),
                new StreetField(29, "Berlin", 7, 280, 150, new int[]{24, 120, 360, 850, 1025, 1200}),
                new ActionableField(30, FieldAction.ARRESTED),
                new StreetField(31, "Luxembourg", 8, 300, 200, new int[]{26, 130, 390, 900, 1100, 1275}),
                new StreetField(32, "Brussels", 8, 300, 200, new int[]{26, 130, 390, 900, 1100, 1275}),
                new ActionableField(33, FieldAction.CHANCE),
                new StreetField(34, "Amsterdam", 8, 320, 200, new int[]{28, 150, 450, 1000, 1200, 1400}),
                new CompanyField(35, "British Airways", COMPANY_FIELD_GROUP, 200, 25),
                new ActionableField(36, FieldAction.LUXURY_TAX),
                new StreetField(37, "Paris", 9, 350, 200, new int[]{35, 175, 500, 1100, 1300, 1500}),
                new ActionableField(38, FieldAction.CHANCE),
                new StreetField(39, "London", 9, 400, 200, new int[]{50, 200, 600, 1400, 1700, 2000})
        );
    }

    private Map<Integer, List<PurchasableField>> defineGroupsMap(List<GameField> fields) {
        Map<Integer, List<PurchasableField>> groups = new HashMap<>();
        for (Object field : fields) {
            if (field instanceof PurchasableField) {
                var purchasableField = (PurchasableField) field;
                int groupId = purchasableField.getGroupId();
                if (!groups.containsKey(groupId)) {
                    ArrayList<PurchasableField> purchasableFields = new ArrayList<>();
                    purchasableFields.add(purchasableField);
                    groups.put(groupId, purchasableFields);
                } else {
                    groups.get(groupId).add(purchasableField);
                }
            }
        }
        return Collections.unmodifiableMap(groups);
    }

    private Map<Integer, Boolean> createHousePurchaseMadeMap(Map<Integer, List<PurchasableField>> groups) {
        Map<Integer, Boolean> housePurchaseMade = new HashMap<>();
        groups.keySet().forEach(key -> housePurchaseMade.put(key, false));
        return housePurchaseMade;
    }
}