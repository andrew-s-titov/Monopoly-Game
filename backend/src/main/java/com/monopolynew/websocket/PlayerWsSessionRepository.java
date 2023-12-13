package com.monopolynew.websocket;

import jakarta.websocket.Session;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PlayerWsSessionRepository {

    private final Map<UUID, Session> activePlayerSessions = new HashMap<>();

    public void addPlayerSession(UUID playerId, Session session) {
        activePlayerSessions.put(playerId, session);
    }

    public void removePlayerSession(UUID playerId) {
        activePlayerSessions.remove(playerId);
    }

    public void refreshPlayerSession(UUID playerId, Session session) {
        Session playerSession = activePlayerSessions.get(playerId);
        if (playerSession == null || !playerSession.equals(session)) {
            activePlayerSessions.put(playerId, session);
        }
    }

    @Nullable
    public Session getPlayerSession(UUID playerId) {
        return activePlayerSessions.get(playerId);
    }

    @NonNull
    public Collection<Session> getAllSessions() {
        return activePlayerSessions.values();
    }
}