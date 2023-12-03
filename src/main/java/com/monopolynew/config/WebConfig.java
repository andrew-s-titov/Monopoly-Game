package com.monopolynew.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "https://*.ngrok.io",
                        "https://*.ngrok-free.app",
                        "http://localhost:3000",
                        "*"
                )
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
