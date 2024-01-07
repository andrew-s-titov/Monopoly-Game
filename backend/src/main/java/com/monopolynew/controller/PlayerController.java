package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game/player")
public class PlayerController {

    private final GameService gameService;

    @PutMapping("/give_up")
    public void giveUp(@RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.giveUp(playerId);
    }
}
