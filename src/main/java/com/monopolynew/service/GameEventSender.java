package com.monopolynew.service;

public interface GameEventSender {

    void sendToAllPlayers(Object gameEvent);

    void sendToPlayer(String playerId, Object gameEvent);
}