package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiceResultEvent implements WebsocketEvent {

    private final int code = 303;

    private final String playerId;

    private final int firstDice;

    private final int secondDice;
}
