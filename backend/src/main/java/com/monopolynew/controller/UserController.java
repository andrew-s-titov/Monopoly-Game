package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.ActiveGameSessionDTO;
import com.monopolynew.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ActiveGameSessionDTO findActiveGameSession(@RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        return ActiveGameSessionDTO.withId(userService.findActiveGameSession(playerId));
    }
}
