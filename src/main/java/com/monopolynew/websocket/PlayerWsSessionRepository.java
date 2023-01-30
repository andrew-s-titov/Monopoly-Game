package com.monopolynew.websocket;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.websocket.Session;
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

    void clearSession();
}