package com.monopolynew.event;

import com.monopolynew.dto.GameFieldState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FieldStateChangeEvent implements GameEvent {

    private final int code = 307;

    private final List<GameFieldState> changes;
}