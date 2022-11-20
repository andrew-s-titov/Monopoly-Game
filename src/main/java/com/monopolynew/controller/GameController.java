package com.monopolynew.controller;

import com.monopolynew.enums.ProposalAction;
import com.monopolynew.enums.JailAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @PostMapping
    public void start() {
        gameService.startGame();
    }

    @GetMapping("/name/{username}")
    public ResponseEntity<?> checkName(@PathVariable("username") String playerName) {
        String message = null;
        if (StringUtils.isBlank(playerName) || playerName.length() < 3) {
            message = "Player name must be at least 3 character long";
        } else if (gameService.usernameTaken(playerName)) {
            message = "Username '" + playerName + "' is already taken. Please, choose another one.";
        }
        return message == null ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body(message);
    }

    @GetMapping("/dice/notify")
    public void notifyAboutRolling() {
        gameService.startRolling();
    }

    @GetMapping("/dice/roll")
    public void rollTheDice() {
        gameService.doRollTheDice();
    }

    @GetMapping("/dice/after")
    public void afterDiceAction() {
        gameService.afterDiceAction();
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