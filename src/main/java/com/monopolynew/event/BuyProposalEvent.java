package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BuyProposalEvent implements GameEvent {

    private final int code = 306;

    private final String playerId;

    private final String fieldName;

    private final int price;
}