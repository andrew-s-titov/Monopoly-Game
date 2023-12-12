package com.monopolynew.event;

import com.monopolynew.dto.UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Getter
@RequiredArgsConstructor
public class GameRoomEvent {

    private final List<UserInfo> players;

    private final int code = 100;
}
