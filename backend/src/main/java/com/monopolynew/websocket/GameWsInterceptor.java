package com.monopolynew.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.GAME_ID_KEY;
import static com.monopolynew.config.GlobalConfig.USER_ID_HEADER;

@Component
@Slf4j
public class GameWsInterceptor extends BaseWsInterceptor {

    public GameWsInterceptor() {
        super(3);
    }

    @Override
    public void manipulateRequest(List<String> pathSegments, Map<String, Object> attributes) throws Exception {
        var gameId = UUID.fromString(pathSegments.get(1));
        var userId = UUID.fromString(pathSegments.get(2));
        attributes.put(USER_ID_HEADER, userId);
        attributes.put(GAME_ID_KEY, gameId);
    }
}
