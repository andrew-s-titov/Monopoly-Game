package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankruptcyEvent implements GameEvent {

    private final int code = 311;

    private final String playerId;
}
