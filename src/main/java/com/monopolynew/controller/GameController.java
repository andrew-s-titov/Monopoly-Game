package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.PlayerStatusDTO;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.exception.PlayerInvalidInputException;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @PostMapping
    public void start() {
        gameService.startGame();
    }

    @GetMapping
    public ResponseEntity<?> checkName(@RequestParam("name") String playerName) {
        String message = null;
        if (StringUtils.isBlank(playerName) || playerName.length() < 3 || playerName.length() > 20) {
            message = "Player name length must be from 3 to 20 characters";
        } else if (gameService.usernameTaken(playerName)) {
            message = "Username '" + playerName + "' is already taken. Please, choose another one.";
        }
        if (message != null) {
            throw new PlayerInvalidInputException(message);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public PlayerStatusDTO status(@CookieValue(value = GlobalConfig.PLAYER_ID_KEY) String playerIdCookie) {
        if (gameService.isGameStarted()) {
            var playerName = gameService.getPlayerName(playerIdCookie);
            return new PlayerStatusDTO(playerName);
        }
        return new PlayerStatusDTO(null);
    }

    @GetMapping("/dice/roll")
    public void startDiceRolling() {
        gameService.startDiceRolling();
    }

    @GetMapping("/dice/result")
    public void broadcastDiceResult() {
        gameService.broadcastDiceResult();
    }

    @GetMapping("/dice/after")
    public void afterDiceAction() {
        gameService.afterDiceRollAction();
    }

    @GetMapping("/after_move")
    public void afterPlayerMoveAction() {
        gameService.afterPlayerMoveAction();
    }

    @GetMapping("/buy")
    public void processBuyProposal(@RequestParam("action") ProposalAction action) {
        gameService.processBuyProposal(action);
    }

    @GetMapping("/pay")
    public void processPayment() {
        gameService.processPayment();
    }

    @GetMapping("/auction/buy")
    public void processAuctionBuyProposal(@RequestParam("action") ProposalAction action) {
        gameService.processAuctionBuyProposal(action);
    }

    @GetMapping("/auction/raise")
    public void processAuctionRaiseProposal(@RequestParam("action") ProposalAction action) {
        gameService.processAuctionRaiseProposal(action);
    }

    @GetMapping("/jail")
    public void processJailAction(@RequestParam("action") JailAction jailAction) {
        gameService.processJailAction(jailAction);
    }
}