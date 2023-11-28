package com.monopolynew.event;

import com.monopolynew.dto.PlayerShortInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Getter
@RequiredArgsConstructor
public class GameRoomEvent {

    private final List<PlayerShortInfo> players;

    private final int code = 100;
}
