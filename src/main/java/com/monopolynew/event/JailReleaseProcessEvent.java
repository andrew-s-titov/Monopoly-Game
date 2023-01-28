package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.game.Rules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JailReleaseProcessEvent implements GameEvent {

    private final int code = 308;

    private final String playerId;

    private final int bail = Rules.JAIL_BAIL;

    private final boolean bailAvailable;
}