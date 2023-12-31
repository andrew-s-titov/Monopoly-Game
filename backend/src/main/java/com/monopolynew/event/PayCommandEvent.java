package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PayCommandEvent implements GameEvent {

    private final int code = 312;

    private final int sum;

    private final boolean wiseToGiveUp;
}
