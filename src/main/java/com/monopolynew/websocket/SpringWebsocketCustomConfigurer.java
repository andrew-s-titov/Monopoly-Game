package com.monopolynew.websocket;

import com.monopolynew.config.GlobalConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.UUID;

@Component
public class SpringWebsocketCustomConfigurer extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringWebsocketCustomConfigurer.context = applicationContext;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> classType) {
        return context.getBean(classType);
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        List<String> cookieHeader = request.getHeaders().get("Cookie");
        String playerIdCookieValue = findPlayerIdCookieValue(cookieHeader);
        String playerId = playerIdCookieValue != null ? playerIdCookieValue : UUID.randomUUID().toString();
        sec.getUserProperties().put(GlobalConfig.PLAYER_ID_KEY, playerId);
        super.modifyHandshake(sec, request, response);
    }

    @Nullable
    private String findPlayerIdCookieValue(List<String> cookieHeader) {
        if (!CollectionUtils.isEmpty(cookieHeader)) {
            for (String cookieString : cookieHeader) {
                String[] cookies = cookieString.split("; ");
                for (String cookie : cookies) {
                    if (cookie.startsWith(GlobalConfig.PLAYER_ID_KEY)) {
                        String cookieValue = cookie.split("=")[1];
                        return cookieValue.equalsIgnoreCase("null") ? null : cookieValue;
                    }
                }
            }
        }
        return null;
    }
}