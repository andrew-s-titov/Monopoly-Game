package com.monopolynew.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.websocket.PlayerWsSessionRepository;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class GameEventSenderWebsocketImpl implements GameEventSender {

    private final PlayerWsSessionRepository playerWsSessionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendToAllPlayers(Object gameEvent) {
        playerWsSessionRepository.getAllSessions()
                .forEach(session -> sendToSession(session, gameEvent));
    }

    @Override
    public void sendToPlayer(String playerId, Object gameEvent) {
        Session wsSession = playerWsSessionRepository.getPlayerSession(playerId);
        if (wsSession != null) {
            sendToSession(wsSession, gameEvent);
        } else {
            log.debug("no session found for player with id {} on this server", playerId);
        }
    }

    private void sendToSession(Session session, Object payload) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(payload));
            } catch (IOException ex) {
                log.error("En error occurred during writing object to websocket:", ex);
            }
        }
    }
}