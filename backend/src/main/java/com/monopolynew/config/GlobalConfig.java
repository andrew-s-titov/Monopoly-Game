package com.monopolynew.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class GlobalConfig {

    public static final String PLAYER_ID_KEY = "player_id";
    public static final String PLAYER_NAME_KEY = "player_name";
    public static final String PLAYER_AVATAR_KEY = "player_avatar";

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jacksonBootDefault) {
        return jacksonBootDefault.build();
    }
}
