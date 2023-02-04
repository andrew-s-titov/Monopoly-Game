package com.monopolynew.map;

import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PurchasableFieldGroups {

    public static final int COMPANY_FIELD_GROUP = 1;
    public static final int UTILITY_FIELD_GROUP = 4;

    private static final Map<Integer, List<Integer>> FIELD_GROUPS = Map.of(
            0, List.of(1, 3),
            COMPANY_FIELD_GROUP, List.of(5, 15, 25, 35),
            2, List.of(6, 8, 9),
            3, List.of(11, 13, 14),
            UTILITY_FIELD_GROUP, List.of(12, 28),
            5, List.of(16, 18, 19),
            6, List.of(21, 23, 24),
            7, List.of(26, 27, 29),
            8, List.of(31, 32, 34),
            9, List.of(37, 39)
    );

    public static List<PurchasableField> getGroupByFieldIndex(Game game, int purchasableFieldIndex) {
        int groupId = getGroupIdByFieldIndex(purchasableFieldIndex);
        return getGroup(game, groupId);
    }

    public static List<PurchasableField> getGroupById(Game game, int groupId) {
        return getGroup(game, groupId);
    }

    public static int getGroupIdByFieldIndex(int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex > Rules.LAST_FIELD_INDEX) {
            throw new IllegalArgumentException("No field exists with index " + fieldIndex);
        }
        for (Map.Entry<Integer, List<Integer>> group : FIELD_GROUPS.entrySet()) {
            List<Integer> fieldsInTheGroup = group.getValue();
            if (fieldsInTheGroup.contains(fieldIndex)) {
                return group.getKey();
            }
        }
        throw new IllegalArgumentException("No group exists for index " + fieldIndex);
    }

    private static List<PurchasableField> getGroup(Game game, int fieldGroupId) {
        if (fieldGroupId < 0 || fieldGroupId > 9) {
            throw new IllegalArgumentException("No group exists for id " + fieldGroupId);
        }
        return FIELD_GROUPS.get(fieldGroupId).stream()
                .map(fieldIndex -> (PurchasableField) game.getGameMap().getField(fieldIndex))
                .collect(Collectors.toList());
    }

    public static Set<Integer> getGroupKeys() {
        return FIELD_GROUPS.keySet();
    }
}