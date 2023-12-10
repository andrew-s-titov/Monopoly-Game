package com.monopolynew.websocket;

import com.monopolynew.config.GlobalConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
        Map<String, List<String>> parameterMap = request.getParameterMap();
        Map<String, Object> socketUserProps = sec.getUserProperties();
        copyParamToSocketProp(parameterMap, socketUserProps, GlobalConfig.PLAYER_ID_KEY);
        copyParamToSocketProp(parameterMap, socketUserProps, GlobalConfig.PLAYER_NAME_KEY);
        copyParamToSocketProp(parameterMap, socketUserProps, GlobalConfig.PLAYER_AVATAR_KEY);
        super.modifyHandshake(sec, request, response);
    }

    private void copyParamToSocketProp(Map<String, List<String>> params, Map<String, Object> props, String paramName) {
        List<String> paramsByName = params.get(paramName);
        if (!CollectionUtils.isEmpty(paramsByName)) {
            props.put(
                    paramName,
                    paramsByName.get(0));
        }
    }
}