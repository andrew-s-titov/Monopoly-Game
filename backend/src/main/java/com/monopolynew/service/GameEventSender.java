package com.monopolynew.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.websocket.UserWsSessionRepository;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
@Component
public class GameEventSender {

    private final UserWsSessionRepository userWsSessionRepository;
    private final ObjectMapper objectMapper;

    public void sendToAllPlayers(Object gameEvent) {
        handleGameEvent(gameEvent,
                event -> userWsSessionRepository.getAllSessions()
                        .forEach(session -> sendToSession(session, event)));
    }

    public void sendToPlayer(UUID playerId, Object gameEvent) {
        Session wsSession = userWsSessionRepository.getUserSession(playerId);
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