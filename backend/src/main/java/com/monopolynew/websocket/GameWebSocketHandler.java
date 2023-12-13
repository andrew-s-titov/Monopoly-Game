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
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.PLAYER_ID_KEY;

@RequiredArgsConstructor
@Slf4j
@Component
// TODO: define path as '/ws/{gameId} to choose game, refactor session repo as game<->session map
@ServerEndpoint(value = "/ws", configurator = SpringWebsocketCustomConfigurer.class)
public class GameWebSocketHandler {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameMapRefresher gameMapRefresher;
    private final PlayerWsSessionRepository playerWsSessionRepository;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        var playerId = getUserIdFromSession(session);
        GameUser user = userRepository.getUser(playerId);

        Game game = gameRepository.getGame();
        if (game.isInProgress()) {
            if (game.playerExists(playerId)) {
                playerWsSessionRepository.refreshPlayerSession(playerId, session);
                gameMapRefresher.restoreGameStateForPlayer(game, playerId);
            } else {
                gameEventSender.sendToPlayer(playerId, new ConnectionErrorEvent("Game in progress"));
            }
            return;
        }

        var allActiveSessions = playerWsSessionRepository.getAllSessions();
        if (!CollectionUtils.isEmpty(allActiveSessions) && allActiveSessions.size() >= Rules.MAX_PLAYERS) {
            gameEventSender.sendToPlayer(playerId,
                    new ConnectionErrorEvent("Only " + Rules.MAX_PLAYERS + " are allowed to play"));
            return;
        }

        playerWsSessionRepository.addPlayerSession(playerId, session);
        game.addPlayer(Player.fromUser(user));
        gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        var playerId = getUserIdFromSession(session);
        var newMessage = ChatMessageEvent.builder()
                .message(message)
                .playerId(playerId)
                .build();
        gameEventSender.sendToAllPlayers(newMessage);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        var playerId = getUserIdFromSession(session);
        var game = gameRepository.getGame();
        // if a game isn't in progress - send an event for other players about disconnection
        if (!game.isInProgress()) {
            game.removePlayer(playerId);
            playerWsSessionRepository.removePlayerSession(playerId);
            gameEventSender.sendToAllPlayers(gameEventGenerator.gameRoomEvent(game));
        }
        // if a game is in progress - do not remove to let the player reconnect with another session
        CloseReason.CloseCode closeCode = reason.getCloseCode();
        if (!closeCode.equals(CloseReason.CloseCodes.NORMAL_CLOSURE)
                && !closeCode.equals(CloseReason.CloseCodes.GOING_AWAY)) {
            log.warn("Not a normal websocket close for user {}, reason: {}", playerId, reason);
        }
    }

    @OnError
    public void onError(Session session, Throwable ex) throws Exception {
        var playerId = getUserIdFromSession(session);
        log.error("An error occurred during websocket exchange for user " + playerId, ex);
    }

    private UUID getUserIdFromSession(Session session) {
        return (UUID) session.getUserProperties().get(PLAYER_ID_KEY);
    }
}
