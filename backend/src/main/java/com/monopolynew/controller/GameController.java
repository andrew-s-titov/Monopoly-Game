package com.monopolynew.controller;

import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
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