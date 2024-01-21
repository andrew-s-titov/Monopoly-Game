package com.monopolynew.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;

@UtilityClass
@Slf4j
public class WebsocketUtils {

    public static void sendEvent(Collection<WebSocketSession> sessions, ObjectMapper objectMapper, Object eventPayload) {
        var eventJson = serializeEvent(objectMapper, eventPayload);
        if (StringUtils.isNotBlank(eventJson)) {
            sessions.forEach(session -> sendToSession(session, eventJson));
        }
    }

    public static void sendEvent(WebSocketSession session, ObjectMapper objectMapper, Object eventPayload) {
        var eventJson = serializeEvent(objectMapper, eventPayload);
        if (StringUtils.isNotBlank(eventJson)) {
            sendToSession(session, eventJson);
        }
    }

    private static String serializeEvent(ObjectMapper objectMapper, Object eventPayload) {
        try {
            return objectMapper.writeValueAsString(eventPayload);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Failed to serialize to json event=" + eventPayload, jsonProcessingException);
        }
        return null;
    }

    private static void sendToSession(WebSocketSession session, String payload) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ex) {
                log.error("Failed to send message to websocket:", ex);
            }
        }
    }
}
