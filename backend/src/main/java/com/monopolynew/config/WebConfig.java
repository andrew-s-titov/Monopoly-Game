package com.monopolynew.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.monopolynew.config.GlobalConfig.ALLOWED_ORIGINS_PATTERNS;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(ALLOWED_ORIGINS_PATTERNS)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
