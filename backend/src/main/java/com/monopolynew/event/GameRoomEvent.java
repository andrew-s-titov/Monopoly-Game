package com.monopolynew.event;

import com.monopolynew.dto.PlayerGameRoomInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Getter
@RequiredArgsConstructor
public class GameRoomEvent {

    private final List<PlayerGameRoomInfo> players;

    private final int code = 100;
}
