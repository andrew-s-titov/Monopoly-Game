package com.monopolynew.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.USER_ID_HEADER;

@Component
@Slf4j
public class GameWsInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        MultiValueMap<String, String> handshakeQueryParams = UriComponentsBuilder
                .fromUri(request.getURI())
                .build()
                .getQueryParams();
        var userId = UUID.fromString(handshakeQueryParams.get(USER_ID_HEADER).get(0));
//        var gameId = UUID.fromString(handshakeQueryParams.get(GAME_ID_KEY).get(0));
        if (ObjectUtils.anyNull(userId)) {
            log.warn("Failed to connect to websocket - not enough params");
            return false;
        }
        attributes.put(USER_ID_HEADER, userId);
//        attributes.put(GAME_ID_KEY, gameId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }

    public static UUID extractUserId(WebSocketSession session) {
        return (UUID) session.getAttributes().get(USER_ID_HEADER);
    }
}
