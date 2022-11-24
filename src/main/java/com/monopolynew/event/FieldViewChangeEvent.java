package com.monopolynew.event;

import com.monopolynew.dto.GameFieldView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FieldViewChangeEvent implements WebsocketEvent {

    private final int code = 307;

    private final List<GameFieldView> changes;
}