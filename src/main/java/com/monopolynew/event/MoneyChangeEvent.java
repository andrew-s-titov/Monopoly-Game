package com.monopolynew.event;

import com.monopolynew.dto.MoneyState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MoneyChangeEvent implements GameEvent {

    private final int code = 305;

    private final List<MoneyState> changes;
}
