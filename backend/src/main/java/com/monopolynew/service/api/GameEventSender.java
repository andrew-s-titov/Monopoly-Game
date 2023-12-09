package com.monopolynew.service.api;

public interface GameEventSender {

    void sendToAllPlayers(Object gameEvent);

    void sendToPlayer(String playerId, Object gameEvent);
}