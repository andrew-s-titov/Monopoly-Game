package com.monopolynew.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.USER_ID_HEADER;

@Component
@Slf4j
public class LandingPageWsInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> pathSegments = UriComponentsBuilder
                .fromUri(request.getURI())
                .build()
                .getPathSegments();
        if (CollectionUtils.isEmpty(pathSegments) || pathSegments.size() < 2) {
            log.warn("Failed to connect to websocket - not enough path segments");
            return false;
        }
        var userId = UUID.fromString(pathSegments.get(1));
        attributes.put(USER_ID_HEADER, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
