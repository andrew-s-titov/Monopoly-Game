package com.monopolynew.websocket;

import javax.websocket.Session;
import java.util.List;

public interface PlayerWsSessionRepository {

    void addPlayerSession(String playerId, Session session);

    Session getPlayerSession(String playerId);

    String getPlayerIdBySessionId(String sessionId);

    List<Session> getAllSessions();

    void clearSession();
}