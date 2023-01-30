package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StreetHouseAmountEvent implements GameEvent {

    private final int code = 314;

    private final int field;

    private final int amount;
}