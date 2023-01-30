package com.monopolynew.websocket;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlayerWsSessionRepositoryImpl implements PlayerWsSessionRepository {

    private final Map<String, Session> activePlayerSessions = new HashMap<>();

    @Override
    public void addPlayerSession(String playerId, Session session) {
        activePlayerSessions.put(playerId, session);
    }

    @Override
    public void refreshPlayerSession(String playerId, Session session) {
        Session playerSession = activePlayerSessions.get(playerId);
        if (playerSession == null || !playerSession.equals(session)) {
            activePlayerSessions.put(playerId, session);
        }
    }

    @Override
    @Nullable
    public Session getPlayerSession(String playerId) {
        return activePlayerSessions.get(playerId);
    }

    @Override
    @Nullable
    public String getPlayerIdBySessionId(String sessionId) {
        return activePlayerSessions.entrySet().stream()
                .filter(playerSession -> playerSession.getValue().getId().equals(sessionId))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    @Override
    @NonNull
    public Collection<Session> getAllSessions() {
        return activePlayerSessions.values();
    }

    @Override
    public void clearSession() {
        activePlayerSessions.clear();
    }
}