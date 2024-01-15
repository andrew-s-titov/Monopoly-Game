package com.monopolynew.websocket;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GamePlayerWsSessionRepository {

    private final Map<UUID, WebSocketSession> activeUserSessions = new HashMap<>();

    public void addUserSession(@NonNull UUID userId, @NonNull WebSocketSession session) {
        activeUserSessions.put(userId, session);
    }

    public void removeUserSession(@NonNull UUID userId) {
        activeUserSessions.remove(userId);
    }

    @Nullable
    public WebSocketSession getUserSession(UUID userId) {
        return activeUserSessions.get(userId);
    }

    @NonNull
    public Collection<WebSocketSession> getAllSessions() {
        return activeUserSessions.values();
    }
}
