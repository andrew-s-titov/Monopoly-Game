package com.monopolynew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.websocket.GamePlayerWsSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.monopolynew.util.WebsocketUtils.sendEvent;

@RequiredArgsConstructor
@Slf4j
@Component
public class GameEventSender {

    private final GamePlayerWsSessionRepository userSessionRepository;
    private final ObjectMapper objectMapper;

    public void sendToAllPlayers(UUID gameId, Object gameEvent) {
        sendEvent(userSessionRepository.getAllSessions(gameId), objectMapper, gameEvent);
    }

    public void sendToPlayer(UUID gameId, UUID playerId, Object gameEvent) {
        var wsSession = userSessionRepository.getUserSession(gameId, playerId);
        if (wsSession == null) {
            log.warn("No session was found for playerId={} on this server", playerId);
            return;
        }
        sendEvent(wsSession, objectMapper, gameEvent);
    }
}