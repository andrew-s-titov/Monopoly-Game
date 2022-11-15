package com.monopolynew.event;

import com.monopolynew.dto.StreetNewOwnerChange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PropertyNewOwnerChangeEvent implements WebsocketEvent {

    private final int code = 307;

    private final List<StreetNewOwnerChange> changes;
}