package com.monopolynew.event;

import com.monopolynew.map.StreetField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StreetHouseAmountEvent implements WebsocketEvent {

    private final int code = 314;

    private final int field;

    private final int amount;

    public static StreetHouseAmountEvent fromStreetField(StreetField field) {
        return new StreetHouseAmountEvent(field.getId(), field.getHouses());
    }
}