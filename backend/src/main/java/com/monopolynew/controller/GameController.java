package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.CreateGameResponseDTO;
import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public CreateGameResponseDTO newGame(@RequestBody NewGameParamsDTO newGameParamsDTO) {
        return new CreateGameResponseDTO(gameService.newGame(newGameParamsDTO));
    }

    @PostMapping
    public void start() {
        gameService.startGame();
    }

    @PutMapping("/turn")
    public void startTurn() {
        gameService.makeUsualTurn();
    }

    @PutMapping("/buy")
    public void processBuyProposal(@RequestParam("action") ProposalAction action) {
        gameService.processBuyProposal(action);
    }

    @PutMapping("/pay")
    public void processPayment() {
        gameService.processPayment();
    }

    @PutMapping("/auction/buy")
    public void processAuctionBuyProposal(@RequestParam("action") ProposalAction action) {
        gameService.processAuctionBuyProposal(action);
    }

    @PutMapping("/auction/raise")
    public void processAuctionRaiseProposal(@RequestParam("action") ProposalAction action) {
        gameService.processAuctionRaiseProposal(action);
    }

    @PutMapping("/jail")
    public void processJailAction(@RequestParam("action") JailAction jailAction) {
        gameService.processJailAction(jailAction);
    }

    @PutMapping("/give_up")
    public void giveUp(@RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.giveUp(playerId);
    }
}