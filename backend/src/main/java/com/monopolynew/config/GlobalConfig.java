package com.monopolynew.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class GlobalConfig {

    public static final String USER_ID_HEADER = "user_id";
    public static final String GAME_ID_KEY = "game_id";

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jacksonBootDefault) {
        return jacksonBootDefault.build();
    }

    @Bean
    public ScheduledExecutorService scheduler() {
        return new ScheduledThreadPoolExecutor(5);
    }
}
