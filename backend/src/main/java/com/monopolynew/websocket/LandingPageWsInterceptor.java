package com.monopolynew.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.monopolynew.config.GlobalConfig.USER_ID_HEADER;

@Component
@Slf4j
public class LandingPageWsInterceptor extends BaseWsInterceptor {

    public LandingPageWsInterceptor() {
        super(2);
    }

    @Override
    public void manipulateRequest(List<String> pathSegments, Map<String, Object> attributes) throws Exception {
        var userId = UUID.fromString(pathSegments.get(1));
        attributes.put(USER_ID_HEADER, userId);
    }
}
