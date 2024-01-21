package com.monopolynew.websocket;

import com.monopolynew.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class LandingPageWebSocketHandler extends AbstractWebSocketHandler {

    private final LandingPageWsSessionRepository landingPageWsSessionRepository;
    private final GameRoomService gameRoomService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        landingPageWsSessionRepository.addUserSession(session);
        gameRoomService.sendGameRooms(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) throws Exception {
        landingPageWsSessionRepository.removeUserSession(session);
        int statusCode = status.getCode();
        if (statusCode != CloseStatus.NORMAL.getCode() && statusCode != CloseStatus.GOING_AWAY.getCode()) {
            log.warn("Not a normal websocket close: status=({}), sessionId={}, attributes=({})",
                    status, session.getId(), session.getAttributes());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("Landing page WS error, sessionId=%s, attributes=(%s)"
                        .formatted(session.getId(), session.getAttributes()),
                exception);
    }

    @Override
    public void handleMessage(WebSocketSession session, @NonNull WebSocketMessage<?> message)
            throws InterruptedException, IOException {
        session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Messages not supported"));
    }
}
