package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class NewPlayerTurn implements GameEvent {

    private final int code = 314;

    private final UUID playerId;
}
