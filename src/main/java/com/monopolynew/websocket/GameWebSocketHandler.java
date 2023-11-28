package com.monopolynew.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.config.GlobalConfig;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.ErrorEvent;
import com.monopolynew.event.GameRoomEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.mapper.PlayerMapper;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameMapRefresher;
import com.monopolynew.service.GameRepository;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
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

@RequiredArgsConstructor
@Slf4j
@Component
@ServerEndpoint(value = "/", configurator = SpringWebsocketCustomConfigurer.class)
public class GameWebSocketHandler {

    public static final int GAME_OVER_CLOSE_REASON_CODE = 3000;

    private final ObjectMapper objectMapper;
    private final PlayerMapper playerMapper;
    private final GameRepository gameRepository;
    private final GameMapRefresher gameMapRefresher;
    private final PlayerWsSessionRepository playerWsSessionRepository;
    private final GameEventSender gameEventSender;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        var playerId = (String) config.getUserProperties().get(GlobalConfig.PLAYER_ID_KEY);
        var playerName = (String) config.getUserProperties().get(GlobalConfig.PLAYER_NAME_KEY);

        Game game = gameRepository.getGame();
        if (game.isInProgress()) {
            if (game.playerExists(playerId)) {
                playerWsSessionRepository.refreshPlayerSession(playerId, session);
                gameMapRefresher.restoreGameStateForPlayer(game, playerId);
            } else {
                String message = "Game in progress";
                closeSessionForViolation(session, message);
            }
            return;
        }

        var allActiveSessions = playerWsSessionRepository.getAllSessions();
        if (!CollectionUtils.isEmpty(allActiveSessions) && allActiveSessions.size() >= Rules.MAX_PLAYERS) {
            gameEventSender.sendToPlayer(playerId, new ErrorEvent("Only " + Rules.MAX_PLAYERS + " are allowed to play"));
            closeSessionForViolation(session, "Max players reached");
            return;
        }

        playerWsSessionRepository.addPlayerSession(playerId, session);
        game.addPlayer(Player.newPlayer(playerId, playerName));
        gameEventSender.sendToAllPlayers(
                new GameRoomEvent(playerMapper.toPlayersShortInfoList(game.getPlayers())));
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ChatMessageEvent playerMessage = objectMapper.readValue(message, ChatMessageEvent.class);
            gameEventSender.sendToAllPlayers(playerMessage);
        } catch (IOException e) {
            // TODO: process exception gracefully
            throw new RuntimeException(e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        var sessionId = session.getId();
        CloseReason.CloseCode closeCode = reason.getCloseCode();
        if (closeCode.equals(CloseReason.CloseCodes.NORMAL_CLOSURE) || closeCode.equals(CloseReason.CloseCodes.GOING_AWAY)) {
            var game = gameRepository.getGame();
            // if a game isn't in progress - send an event for other players about disconnection
            if (!game.isInProgress()) {

                var playerId = playerWsSessionRepository.getPlayerIdBySessionId(session.getId());
                if (playerId != null) {
                    game.removePlayer(playerId);
                    gameEventSender.sendToAllPlayers(
                            new GameRoomEvent(playerMapper.toPlayersShortInfoList(game.getPlayers())));
                }
            }
            // if a game is in progress - do not remove to let the player reconnect with another session
        } else if (closeCode.equals(CloseReason.CloseCodes.VIOLATED_POLICY)) {
            log.debug("Forced websocket connection close for session {}", sessionId);
        } else if (closeCode.getCode() == GAME_OVER_CLOSE_REASON_CODE) {
            log.debug("Websocket connection closed on game over {}", sessionId);
        } else {
            log.warn("Not a normal websocket close on session {}", sessionId);
            // TODO: remove player or anything in other cases??
        }
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        String sessionId = session.getId();
        log.error("An error occurred during websocket exchange within session " + sessionId, ex);
    }

    private void closeSessionForViolation(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, message));
        }
    }
}