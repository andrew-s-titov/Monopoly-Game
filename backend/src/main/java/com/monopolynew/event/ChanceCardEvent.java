package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChanceCardEvent implements GameEvent {

    private final int code = 202;

    private final String text;
}
