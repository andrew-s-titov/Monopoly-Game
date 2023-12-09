package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GameOverEvent implements GameEvent {

    private final int code = 315;

    private final String winnerName;
}