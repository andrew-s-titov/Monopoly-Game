package com.monopolynew.websocket;

import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ConnectionErrorEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.service.api.GameEventGenerator;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.service.api.GameMapRefresher;
import com.monopolynew.service.api.GameRepository;
import com.monopolynew.user.GameUser;
import com.monopolynew.user.UserRepository;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.WsSession;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
// TODO: define path as '/{gameId} to choose game, refactor session repo as game<->session map
@ServerEndpoint(value = "/{userId}", configurator = SpringWebsocketCustomConfigurer.class)
public class GameWebSocketHandler {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameMapRefresher gameMapRefresher;
    private final UserWsSessionRepository userWsSessionRepository;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        var playerId = UUID.fromString(userId);
        GameUser user = userRepository.getUser(playerId);

        Game game = gameRepository.getGame();
        if (game.isInProgress()) {
            if (game.playerExists(playerId)) {
                userWsSessionRepository.addUserSession(playerId, session);
                gameMapRefresher.restoreGameStateForPlayer(game, playerId);
            } else {
                gameEventSender.sendToPlayer(playerId, new ConnectionErrorEvent("Game in progress"));
            }
            return;
        }

        var allActiveSessions = userWsSessionRepository.getAllSessions();
        if (!CollectionUtils.isEmpty(allActiveSessions) && allActiveSessions.size() >= Rules.MAX_PLAYERS) {
            gameEventSender.sendToPlayer(playerId,
                    new ConnectionErrorEvent("Only " + Rules.MAX_PLAYERS + " are allowed to play"));
            return;
        }

        userWsSessionRepository.addUserSession(playerId, session);
        game.addPlayer(Player.fromUser(user));
        gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("userId") String userId) {
        var playerId = UUID.fromString(userId);
        var newMessage = ChatMessageEvent.builder()
                .message(message)
                .playerId(playerId)
                .build();
        gameEventSender.sendToAllPlayers(newMessage);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("userId") String userId) {
        UUID playerId = UUID.fromString(userId);
        var game = gameRepository.getGame();
        // if a game isn't in progress - send an event for other players about disconnection
        if (!game.isInProgress()) {
            game.removePlayer(playerId);
            userWsSessionRepository.removeUserSession(playerId);
            gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
        }
        // if a game is in progress - do not remove to let the player reconnect with another session
        CloseReason.CloseCode closeCode = reason.getCloseCode();
        if (!closeCode.equals(CloseReason.CloseCodes.NORMAL_CLOSURE)
                && !closeCode.equals(CloseReason.CloseCodes.GOING_AWAY)) {
            String httpSessionId = null;
            if (session instanceof WsSession wsSession) {
                httpSessionId = wsSession.getHttpSessionId();
            }
            log.warn("Not a normal websocket close: userId={}, reason={}, sessionId={}, httpSessionId={}",
                    playerId, reason, session.getId(), httpSessionId);
        }
    }

    @OnError
    public void onError(Session session, Throwable ex, @PathParam("userId") String userId) throws Exception {
        log.error("An error occurred during websocket connection for userId " + userId, ex);
    }
}
