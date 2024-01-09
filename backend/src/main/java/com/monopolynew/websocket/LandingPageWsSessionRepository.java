package com.monopolynew.websocket;

import com.monopolynew.config.WebSocketConfig;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class LandingPageWsSessionRepository {

    private final Map<UUID, WebSocketSession> activeUserSessions = new HashMap<>();

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
