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
public class UserWsSessionRepository {

    private final Map<UUID, Session> activeUserSessions = new HashMap<>();

    public void addUserSession(@NonNull UUID userId, @NonNull Session session) {
        activeUserSessions.put(userId, session);
    }

    public void removeUserSession(@NonNull UUID userId) {
        activeUserSessions.remove(userId);
    }

    @Nullable
    public UUID getUserIdBySession(@NonNull Session session) {
        return activeUserSessions.entrySet().stream()
                .filter(userSession -> userSession.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    @Nullable
    public Session getUserSession(UUID userId) {
        return activeUserSessions.get(userId);
    }

    @NonNull
    public Collection<Session> getAllSessions() {
        return activeUserSessions.values();
    }
}