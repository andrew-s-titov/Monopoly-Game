package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChanceCardEvent implements GameEvent {

    private final int code = 313;

    private final String text;
}