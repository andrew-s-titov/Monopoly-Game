package com.monopolynew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class MonopolyNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonopolyNewApplication.class, args);
    }
}