package com.monopolynew.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.websocket.PlayerWsSessionRepository;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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

    @Override
    public void closeExchangeChannel(@Nullable CloseReason reason) {
        var allActiveSessions = playerWsSessionRepository.getAllSessions();
        for (Session wsSession : allActiveSessions) {
            if (wsSession.isOpen()) {
                try {
                    if (reason != null) {
                        wsSession.close(reason);
                    } else {
                        wsSession.close();
                    }
                } catch (IOException ex) {
                    log.error("Failed to close session: ", ex);
                }
            }
        }
        playerWsSessionRepository.clearSessions();
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