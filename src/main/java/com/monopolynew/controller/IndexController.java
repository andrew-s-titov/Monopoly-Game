package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.exception.PlayerInvalidInputException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class IndexController {

    @PostMapping
    public Map<String, String> login(@RequestParam(name = "name") String playerName,
                      HttpServletResponse response) {
        // TODO: remove upon login feature implementation
        if (StringUtils.isBlank(playerName) || playerName.length() < 3 || playerName.length() > 20) {
            throw new PlayerInvalidInputException("Player name length must be from 3 to 20 characters");
        }
        return Map.of(
                GlobalConfig.PLAYER_ID_KEY, UUID.randomUUID().toString(),
                GlobalConfig.PLAYER_NAME_KEY, playerName
        );
    }
}