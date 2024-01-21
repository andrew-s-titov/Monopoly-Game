package com.monopolynew.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseWsInterceptor implements HandshakeInterceptor {

    private final int minPathSegments;

    public BaseWsInterceptor(int minPathSegments) {
        this.minPathSegments = minPathSegments;
    }

    abstract void manipulateRequest(List<String> pathSegments, Map<String, Object> attributes) throws Exception;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> pathSegments = UriComponentsBuilder
                .fromUri(request.getURI())
                .build()
                .getPathSegments();
        if (CollectionUtils.isEmpty(pathSegments) || pathSegments.size() < minPathSegments) {
            log.warn("Failed to connect to websocket - not enough path segments");
            return false;
        }
        manipulateRequest(pathSegments, attributes);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
