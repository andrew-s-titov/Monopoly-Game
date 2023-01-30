package com.monopolynew.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.event.GameEvent;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.websocket.PlayerWsSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class GameEventSenderWebsocketImpl implements GameEventSender {

    private final PlayerWsSessionRepository playerWsSessionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendToAllPlayers(GameEvent event) {
        playerWsSessionRepository.getAllSessions()
                .forEach(session -> sendToSession(session, event));
    }

    @Override
    public void sendToPlayer(String playerId, GameEvent event) {
        Session wsSession = playerWsSessionRepository.getPlayerSession(playerId);
        if (wsSession != null) {
            sendToSession(wsSession, event);
        } else {
            log.debug("no session found for player with id {} on this server", playerId);
        }
    }

    @Override
    public void closeExchangeChannel() {
        var allActiveSessions = playerWsSessionRepository.getAllSessions();
        for (Session wsSession : allActiveSessions) {
            if (wsSession.isOpen()) {
                try {
                    wsSession.close();
                } catch (IOException ex) {
                    log.error("Failed to close session: ", ex);
                }
            }
        }
        playerWsSessionRepository.clearSession();
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