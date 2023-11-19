package com.monopolynew.service;

import jakarta.websocket.CloseReason;
import org.springframework.lang.Nullable;

public interface GameEventSender {

    void sendToAllPlayers(Object gameEvent);

    void sendToPlayer(String playerId, Object gameEvent);

    void closeExchangeChannel(@Nullable CloseReason reason);
}