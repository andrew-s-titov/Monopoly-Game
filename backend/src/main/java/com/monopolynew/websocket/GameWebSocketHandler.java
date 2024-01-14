package com.monopolynew.websocket;

import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ConnectionErrorEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameMapRefresher;
import com.monopolynew.service.GameRepository;
import com.monopolynew.service.GameRoomService;
import com.monopolynew.user.GameUser;
import com.monopolynew.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import static com.monopolynew.config.WebSocketConfig.extractUserId;

@RequiredArgsConstructor
@Slf4j
@Component
// TODO: define path as '/{gameId} to choose game, refactor session repo as game<->session map
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final UserWsSessionRepository userSessionRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameMapRefresher gameMapRefresher;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;
    private final GameRoomService gameRoomService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var playerId = extractUserId(session);

        GameUser user = userRepository.getUser(playerId);

        Game game = gameRepository.getGame();
        if (game.isInProgress()) {
            if (game.playerExists(playerId)) {
                userSessionRepository.addUserSession(playerId, session);
                gameMapRefresher.restoreGameStateForPlayer(game, playerId);
            } else {
                gameEventSender.sendToPlayer(playerId, new ConnectionErrorEvent("Game in progress"));
            }
            return;
        }

        var allActiveSessions = userSessionRepository.getAllSessions();
        if (CollectionUtils.isNotEmpty(allActiveSessions) && allActiveSessions.size() >= Rules.MAX_PLAYERS) {
            gameEventSender.sendToPlayer(playerId,
                    new ConnectionErrorEvent("Only " + Rules.MAX_PLAYERS + " are allowed to play"));
            return;
        }

        userSessionRepository.addUserSession(playerId, session);
        game.addPlayer(Player.fromUser(user));
        gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
        gameRoomService.refreshGameRooms();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var playerId = extractUserId(session);
        userSessionRepository.removeUserSession(playerId);
        var game = gameRepository.getGame();
        // if a game isn't in progress - send an event for other players about disconnection
        if (!game.isInProgress()) {
            game.removePlayer(playerId);
            gameRoomService.refreshGameRooms();
            gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
        }
        // if a game is in progress - do not remove from game to let the player reconnect with another session
        int statusCode = status.getCode();
        if (statusCode != CloseStatus.NORMAL.getCode() && statusCode != CloseStatus.GOING_AWAY.getCode()) {
            log.warn("Not a normal websocket close: status=({}), sessionId={}, attributes=({})",
                    status, session.getId(), session.getAttributes());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        var userId = extractUserId(session);
        log.error("Websocket error, userId=" + userId, exception);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        var playerId = extractUserId(session);
        var newMessage = ChatMessageEvent.builder()
                .message(message.getPayload())
                .playerId(playerId)
                .build();
        gameEventSender.sendToAllPlayers(newMessage);
    }
}
