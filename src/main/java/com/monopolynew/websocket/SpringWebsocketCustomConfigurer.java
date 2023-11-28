package com.monopolynew.websocket;

import com.monopolynew.config.GlobalConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

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
        Map<String, Object> socketUserProps = sec.getUserProperties();
        socketUserProps.put(
                GlobalConfig.PLAYER_ID_KEY,
                findCookieValue(cookieHeader, GlobalConfig.PLAYER_ID_KEY));
        socketUserProps.put(
                GlobalConfig.PLAYER_NAME_KEY,
                findCookieValue(cookieHeader, GlobalConfig.PLAYER_NAME_KEY));
        super.modifyHandshake(sec, request, response);
    }

    @Nullable
    private String findCookieValue(List<String> cookieHeader, String cookieName) {
        if (!CollectionUtils.isEmpty(cookieHeader)) {
            for (String cookieString : cookieHeader) {
                String[] cookies = cookieString.split("; ");
                for (String cookie : cookies) {
                    if (cookie.startsWith(cookieName)) {
                        String cookieValue = cookie.split("=")[1];
                        return cookieValue.equalsIgnoreCase("null") ? null : cookieValue;
                    }
                }
            }
        }
        return null;
    }
}