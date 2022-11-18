package com.monopolynew.websocket;

import com.monopolynew.event.WebsocketEvent;

public interface GameEventSender {

    void sendToAllPlayers(WebsocketEvent event);

    void sendToPlayer(String playerId, WebsocketEvent event);

    void closeExchangeChannel();
}