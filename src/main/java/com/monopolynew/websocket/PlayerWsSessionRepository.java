package com.monopolynew.websocket;

import jakarta.websocket.Session;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

public interface PlayerWsSessionRepository {

    void addPlayerSession(String playerId, Session session);

    void refreshPlayerSession(String playerId, Session session);

    @Nullable
    Session getPlayerSession(String playerId);

    @Nullable
    String getPlayerIdBySessionId(String sessionId);

    @NonNull
    Collection<Session> getAllSessions();
}