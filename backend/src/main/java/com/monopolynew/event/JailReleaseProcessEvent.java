package com.monopolynew.event;

import com.monopolynew.game.Rules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class JailReleaseProcessEvent implements GameEvent {

    private final int code = 308;

    private final UUID playerId;

    private final int bail = Rules.JAIL_BAIL;
}