package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PayCommandEvent implements GameEvent {

    private final int code = 312;

    private final String playerId;

    private final int sum;

    private final boolean payable;

    private final boolean wiseToGiveUp;
}