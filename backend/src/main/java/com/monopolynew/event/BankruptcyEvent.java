package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BankruptcyEvent implements GameEvent {

    private final int code = 311;

    private final UUID playerId;
}
