package com.monopolynew.websocket;

public interface GameMessageExchanger {

    void sendToAllPlayers(Object payload);

    void sendToPlayer(String playerId, Object payload);

    void closeExchangeChannel();
}