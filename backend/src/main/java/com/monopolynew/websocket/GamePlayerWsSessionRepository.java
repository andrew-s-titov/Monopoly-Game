package com.monopolynew.websocket;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Component
public class GamePlayerWsSessionRepository {

    private final Map<UUID, Map<UUID, WebSocketSession>> activeGamePlayerSessions = new HashMap<>();

    public void addUserSession(@NonNull UUID gameId, @NonNull UUID userId, @NonNull WebSocketSession session) {
        activeGamePlayerSessions.computeIfAbsent(gameId, id -> new HashMap<>());
        activeGamePlayerSessions.get(gameId).put(userId, session);
    }

    public void removeUserSession(@NonNull UUID gameId, @NonNull UUID userId) {
        Map<UUID, WebSocketSession> playerSessions = activeGamePlayerSessions.get(gameId);
        if (playerSessions != null) {
            playerSessions.remove(userId);
        }
    }

    @Nullable
    public WebSocketSession getUserSession(@NonNull UUID gameId, UUID userId) {
        Map<UUID, WebSocketSession> playerSessions = activeGamePlayerSessions.get(gameId);
        return playerSessions == null ? null : playerSessions.get(userId);
    }

    @NonNull
    public Collection<WebSocketSession> getAllSessions(@NonNull UUID gameId) {
        Map<UUID, WebSocketSession> playerSessions = activeGamePlayerSessions.get(gameId);
        return playerSessions == null ? emptyList() : playerSessions.values();
    }
}
