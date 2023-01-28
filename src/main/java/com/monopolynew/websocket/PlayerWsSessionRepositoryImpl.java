package com.monopolynew.websocket;

import com.monopolynew.dto.PlayerSession;
import com.monopolynew.websocket.PlayerWsSessionRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PlayerWsSessionRepositoryImpl implements PlayerWsSessionRepository {

    private final Map<String, PlayerSession> activeSessions = new HashMap<>();

    @Override
    public void addPlayerSession(String playerId, Session session) {
        activeSessions.put(session.getId(), new PlayerSession(playerId, session));
    }

    @Override
    @Nullable
    public Session getPlayerSession(String playerId) {
        return activeSessions.values().stream()
                .filter(playerSession -> playerSession.getPlayerId().equals(playerId))
                .findAny()
                .map(PlayerSession::getSession)
                .orElse(null);
    }

    @Override
    public String getPlayerIdBySessionId(String sessionId) {
        return activeSessions.get(sessionId).getPlayerId();
    }

    @Override
    @NonNull
    public List<Session> getAllSessions() {
        return activeSessions.values()
                .stream()
                .map(PlayerSession::getSession)
                .collect(Collectors.toList());
    }

    @Override
    public void clearSession() {
        activeSessions.clear();
    }
}