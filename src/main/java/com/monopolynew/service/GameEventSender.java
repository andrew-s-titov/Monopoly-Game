package com.monopolynew.service;

import com.monopolynew.event.GameEvent;

public interface GameEventSender {

    void sendToAllPlayers(GameEvent event);

    void sendToPlayer(String playerId, GameEvent event);

    void closeExchangeChannel();
}