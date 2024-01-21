package com.monopolynew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopolynew.event.AvailableGamesEvent;
import com.monopolynew.websocket.LandingPageWsSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.monopolynew.util.WebsocketUtils.sendEvent;

@Component
@RequiredArgsConstructor
public class GameRoomService {

    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;
    private final LandingPageWsSessionRepository landingPageWsSessionRepository;

    public void refreshGameRooms() {
        sendEvent(landingPageWsSessionRepository.getAllSessions(), objectMapper, event());
    }

    public void sendGameRooms(WebSocketSession session) {
        sendEvent(session, objectMapper, event());
    }

    private AvailableGamesEvent event() {
        return AvailableGamesEvent.from(gameRepository.allGames());
    }
}
