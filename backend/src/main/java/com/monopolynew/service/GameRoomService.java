package com.monopolynew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.dto.GameRoomParticipant;
import com.monopolynew.event.AvailableGamesEvent;
import com.monopolynew.game.Game;
import com.monopolynew.websocket.LandingPageWsSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;

import static com.monopolynew.util.WebsocketUtils.sendEvent;

@Component
@RequiredArgsConstructor
public class GameRoomService {

    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;
    private final LandingPageWsSessionRepository landingPageWsSessionRepository;

    public void refreshGameRooms() {
        // TODO: get all games in not-started state
        sendEvent(landingPageWsSessionRepository.getAllSessions(), objectMapper, event());
    }

    public void sendGameRooms(WebSocketSession session) {
        sendEvent(session, objectMapper, event());
    }

    private AvailableGamesEvent event() {
        Collection<Game> games = gameRepository.allGames();
        if (games.isEmpty()) {
            return new AvailableGamesEvent(Map.of());
        } else {
            var game = games.iterator().next();
            return new AvailableGamesEvent(Map.of(
                    game.getId(),
                    game.getPlayers().stream()
                            .map(player -> new GameRoomParticipant(player.getName(), player.getAvatar()))
                            .toList()
            ));
        }
    }
}
