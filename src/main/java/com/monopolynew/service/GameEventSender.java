package com.monopolynew.service;

import com.monopolynew.event.GameEvent;
import org.springframework.lang.Nullable;

import jakarta.websocket.CloseReason;

public interface GameEventSender {

    void sendToAllPlayers(GameEvent event);

    void sendToPlayer(String playerId, GameEvent event);

    void closeExchangeChannel(@Nullable CloseReason reason);
}