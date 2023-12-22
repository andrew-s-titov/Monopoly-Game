package com.monopolynew.config;

import com.monopolynew.websocket.GameWebSocketHandler;
import com.monopolynew.websocket.GameWsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebsocketHandler;
    private final GameWsInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebsocketHandler, "/ws")
                .setAllowedOriginPatterns(
                        "https://*.ngrok.io",
                        "https://*.ngrok-free.app",
                        "http://localhost:3000")
                .addInterceptors(handshakeInterceptor);
    }
}
