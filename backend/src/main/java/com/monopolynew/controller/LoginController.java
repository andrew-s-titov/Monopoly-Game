package com.monopolynew.controller;

import com.monopolynew.dto.LoginData;
import com.monopolynew.dto.LoginResponse;
import com.monopolynew.exception.PlayerInvalidInputException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class LoginController {

    @PostMapping
    public LoginResponse login(@RequestBody LoginData loginData) {
        String playerName = loginData.getName();
        // TODO: remove upon login feature implementation
        if (StringUtils.isBlank(playerName) || playerName.length() < 3 || playerName.length() > 20) {
            throw new PlayerInvalidInputException("Player name length must be from 3 to 20 characters");
        }
        return new LoginResponse(UUID.randomUUID());
    }
}
