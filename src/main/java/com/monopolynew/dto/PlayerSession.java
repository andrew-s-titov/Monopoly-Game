package com.monopolynew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.websocket.Session;

@AllArgsConstructor
@Getter
public class PlayerSession {

    private final String playerId;
    private final Session session;
}
