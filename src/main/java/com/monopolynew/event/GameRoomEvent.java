package com.monopolynew.event;

import com.monopolynew.dto.PlayerShortInfo;
import lombok.Getter;

import java.util.List;

@Getter
public record GameRoomEvent(List<PlayerShortInfo> players) {

    private static final int code = 100;
}
