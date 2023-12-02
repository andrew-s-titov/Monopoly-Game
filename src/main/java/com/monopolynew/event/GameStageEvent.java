package com.monopolynew.event;

import com.monopolynew.enums.GameStage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GameStageEvent implements GameEvent {

    private final int code = 350;

    private final GameStage gameStage;
}
