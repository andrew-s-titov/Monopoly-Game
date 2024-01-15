package com.monopolynew.config;

import com.monopolynew.websocket.GameWebSocketHandler;
import com.monopolynew.websocket.GameWsInterceptor;
import com.monopolynew.websocket.LandingPageWebSocketHandler;
import com.monopolynew.websocket.LandingPageWsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.GAME_ID_KEY;
import static com.monopolynew.config.GlobalConfig.USER_ID_HEADER;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String[] ALLOWED_ORIGINS = new String[]{
            "https://*.ngrok.io",
            "https://*.ngrok-free.app",
            "http://localhost:3000"
    };

    private final GameWebSocketHandler gameWebsocketHandler;
    private final GameWsInterceptor handshakeInterceptor;

    private final LandingPageWebSocketHandler landingPageWebSocketHandler;
    private final LandingPageWsInterceptor landingPageWsInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebsocketHandler, "/game/**")
                .setAllowedOriginPatterns(ALLOWED_ORIGINS)
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(landingPageWebSocketHandler, "/start/**")
                .setAllowedOriginPatterns(ALLOWED_ORIGINS)
                .addInterceptors(landingPageWsInterceptor);
    }

    public static UUID extractUserId(WebSocketSession session) {
        return (UUID) session.getAttributes().get(USER_ID_HEADER);
    }

    public static UUID extractGameId(WebSocketSession session) {
        return (UUID) session.getAttributes().get(GAME_ID_KEY);
    }
}
