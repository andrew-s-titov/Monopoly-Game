package com.monopolynew.websocket;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.exception.ClientBadRequestException;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
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
        Map<String, List<String>> parameterMap = request.getParameterMap();
        Map<String, Object> socketUserProps = sec.getUserProperties();
        extractUserIdToSocketProps(parameterMap, socketUserProps);
        super.modifyHandshake(sec, request, response);
    }

    private void extractUserIdToSocketProps(Map<String, List<String>> params, Map<String, Object> props) {
        List<String> paramsByName = params.get(GlobalConfig.PLAYER_ID_KEY);
        if (!CollectionUtils.isEmpty(paramsByName)) {
            String playerId = paramsByName.get(0);
            if (StringUtils.isBlank(playerId)) {
                throw new ClientBadRequestException("Player ID has invalid format");
            }
            try {
                var userId = UUID.fromString(playerId);
                props.put(GlobalConfig.PLAYER_ID_KEY, userId);
            } catch (IllegalArgumentException uuidParsException) {
                throw new ClientBadRequestException("Player ID has invalid format");
            }
        }
    }
}