package com.monopolynew.websocket;

import com.monopolynew.config.WebSocketConfig;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LandingPageWsSessionRepository {

    private final Map<UUID, WebSocketSession> activeUserSessions = new ConcurrentHashMap<>();

    public void addUserSession(@NonNull WebSocketSession session) {
        activeUserSessions.put(WebSocketConfig.extractUserId(session), session);
    }

    public void removeUserSession(@NonNull WebSocketSession session) {
        activeUserSessions.remove(WebSocketConfig.extractUserId(session));
    }

    @NonNull
    public Collection<WebSocketSession> getAllSessions() {
        return activeUserSessions.values();
    }
}
