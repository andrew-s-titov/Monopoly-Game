package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.enums.PlayerManagementAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game/player")
public class PlayerController {

    private final GameService gameService;

    @GetMapping("/{subjectPlayerId}/management")
    public List<PlayerManagementAction> availablePlayerManagementActions(
            @PathVariable("subjectPlayerId") String subjectPlayerId,
            @CookieValue(GlobalConfig.PLAYER_ID_KEY) String requestingPlayerId) {
        return gameService.availablePlayerManagementActions(requestingPlayerId, subjectPlayerId);
    }

    @GetMapping("/give_up")
    public void giveUp(@CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        gameService.giveUp(playerId);
    }
}
