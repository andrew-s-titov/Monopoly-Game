package com.monopolynew.service.api;

import java.util.UUID;

public interface GameEventSender {

    void sendToAllPlayers(Object gameEvent);

    void sendToPlayer(UUID playerId, Object gameEvent);
}
