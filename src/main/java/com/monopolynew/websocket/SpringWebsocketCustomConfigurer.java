package com.monopolynew.websocket;

import com.monopolynew.config.GlobalConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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
        Map<String, List<String>> parameterMap = request.getParameterMap();
        Map<String, Object> socketUserProps = sec.getUserProperties();
        socketUserProps.put(
                GlobalConfig.PLAYER_ID_KEY,
                parameterMap.get(GlobalConfig.PLAYER_ID_KEY).get(0));
        socketUserProps.put(
                GlobalConfig.PLAYER_NAME_KEY,
                parameterMap.get(GlobalConfig.PLAYER_NAME_KEY).get(0));
        super.modifyHandshake(sec, request, response);
    }
}