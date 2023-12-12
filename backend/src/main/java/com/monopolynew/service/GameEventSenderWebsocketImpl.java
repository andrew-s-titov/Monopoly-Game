package com.monopolynew.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.websocket.PlayerWsSessionRepository;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
@Component
public class GameEventSenderWebsocketImpl implements GameEventSender {

    private final PlayerWsSessionRepository playerWsSessionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendToAllPlayers(Object gameEvent) {
        handleGameEvent(gameEvent,
                event -> playerWsSessionRepository.getAllSessions()
                        .forEach(session -> sendToSession(session, event)));
    }

    @Override
    public void sendToPlayer(String playerId, Object gameEvent) {
        Session wsSession = playerWsSessionRepository.getPlayerSession(playerId);
        if (wsSession == null) {
            log.warn("No session was found for playerId={} on this server", playerId);
            return;
        }
        handleGameEvent(gameEvent, event -> sendToSession(wsSession, event));
    }

    private void sendToSession(Session session, String payload) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendText(payload);
            } catch (IOException ex) {
                log.error("Failed to send message to websocket:", ex);
            }
        }
    }

    private void handleGameEvent(Object eventPayload, Consumer<String> eventHandler) {
        try {
            String eventJson = objectMapper.writeValueAsString(eventPayload);
            eventHandler.accept(eventJson);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Failed to serialize to json event=" + eventPayload, jsonProcessingException);
        }
    }
}