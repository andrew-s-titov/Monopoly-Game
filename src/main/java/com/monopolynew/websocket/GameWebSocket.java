package com.monopolynew.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.PlayerSession;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ErrorEvent;
import com.monopolynew.event.PlayerConnectedEvent;
import com.monopolynew.event.PlayerDisconnectedEvent;
import com.monopolynew.event.UserIdentificationEvent;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.service.GameHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
@ServerEndpoint(value = "/connect/{username}", configurator = SpringWebsocketCustomConfigurer.class)
public class GameWebSocket implements GameMessageExchanger {

    private final ObjectMapper objectMapper;
    private final GameHolder gameHolder;

    private final Map<String, PlayerSession> activeSessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("username") String username) throws IOException {
        // this ID is generated on first entering and supposed to be past by FE from cookie or storage in header on handshake request
        var playerId = (String) config.getUserProperties().get(GlobalConfig.PLAYER_ID_KEY);
        var sessionId = session.getId();

        if (gameHolder.getGame().isInProgress()) {
            if (gameHolder.getGame().playerExists(playerId)) {
                activeSessions.put(sessionId, new PlayerSession(playerId, session));
                sendToSession(session, gameHolder.getGame().mapRefreshEvent());
            } else {
                String message = "Game in progress";
                closeSessionForViolation(session, message);
            }
            return;
        }

        if (!CollectionUtils.isEmpty(activeSessions)) {
            if (activeSessions.size() >= Rules.MAX_PLAYERS) {
                sendError(session, "Only " + Rules.MAX_PLAYERS + " are allowed to play");
                closeSessionForViolation(session, "Max players reached");
                return;
            }
            // sending a new player event to show other players
            activeSessions.values().stream()
                    .map(playerSession -> gameHolder.getGame().getPlayerById(playerSession.getPlayerId()))
                    .forEach(player -> sendToSession(session, PlayerConnectedEvent.fromPlayer(player)));
        }
        sendToSession(session, new UserIdentificationEvent(playerId));
        var player = Player.newPlayer(playerId, username);
        activeSessions.put(sessionId, new PlayerSession(playerId, session));
        gameHolder.getGame().addPlayer(player);
        sendToAllPlayers(PlayerConnectedEvent.fromPlayer(player));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        var playerId = activeSessions.get(session.getId()).getPlayerId();
        sendToAllPlayers(new ChatMessageEvent(playerId, message));
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        var sessionId = session.getId();
        CloseReason.CloseCode closeCode = reason.getCloseCode();
        if (closeCode.equals(CloseReason.CloseCodes.NORMAL_CLOSURE) || closeCode.equals(CloseReason.CloseCodes.GOING_AWAY)) {
            var playerId = activeSessions.get(sessionId).getPlayerId();
            var player = gameHolder.getGame().getPlayerById(playerId);
            if (player != null && !gameHolder.getGame().isInProgress()) {
                // we don't need to send this if game is in progress
                sendToAllPlayers(PlayerDisconnectedEvent.fromPlayer(player));
                gameHolder.getGame().removePlayer(playerId);
            }
            // if game in progress - do not remove to let reconnect
        } else if (closeCode.equals(CloseReason.CloseCodes.VIOLATED_POLICY)) {
            log.debug("Forced websocket connection close for session {}", sessionId);
        } else {
            log.warn("Not a normal websocket close on session {}", sessionId);
            // TODO: remove player or anything in other cases??
        }
        activeSessions.remove(sessionId);
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        String sessionId = session.getId();
        log.error("An error occurred during websocket exchange within session " + sessionId, ex);
    }

    @Override
    public void sendToAllPlayers(Object payload) {
        if (!CollectionUtils.isEmpty(activeSessions)) {
            activeSessions.values().stream()
                    .map(PlayerSession::getSession)
                    .forEach(session -> sendToSession(session, payload));
        }
    }

    @Override
    public void sendToPlayer(String playerId, Object payload) {
        activeSessions.values().stream()
                .filter(playerSession -> playerSession.getPlayerId().equals(playerId))
                .findAny()
                .ifPresent(playerSession -> sendToSession(playerSession.getSession(), payload));
    }

    @Override
    public void closeExchangeChannel() {
        for (PlayerSession value : activeSessions.values()) {
            Session session = value.getSession();
            if (session.isOpen()) {
                try {
                    session.close();
                } catch (IOException ex) {
                    log.error("Failed to close session: ", ex);
                }
            }
        }
    }

    private void sendError(Session session, String errorMessage) {
        sendToSession(session, new ErrorEvent(errorMessage));
    }

    private void sendToSession(Session session, Object payload) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(payload));
            } catch (IOException ex) {
                log.error("En error occurred during writing object to websocket:", ex);
            }
        }
    }

    private void closeSessionForViolation(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, message));
        }
    }
}
