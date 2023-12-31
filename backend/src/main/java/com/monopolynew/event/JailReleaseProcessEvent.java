package com.monopolynew.event;

import com.monopolynew.game.Rules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JailReleaseProcessEvent implements GameEvent {

    private final int code = 308;

    private final int bail = Rules.JAIL_BAIL;
}
