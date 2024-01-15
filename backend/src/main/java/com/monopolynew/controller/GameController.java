package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.GameResponseDTO;
import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping
    public GameResponseDTO newGame(@RequestBody NewGameParamsDTO newGameParamsDTO) {
        return GameResponseDTO.withId(gameService.newGame(newGameParamsDTO));
    }

    @PostMapping("/{gameId}")
    public void start(@PathVariable("gameId") UUID gameId) {
        gameService.startGame(gameId);
    }

    @PutMapping("/{gameId}/turn")
    public void startTurn(@PathVariable("gameId") UUID gameId) {
        gameService.makeUsualTurn(gameId);
    }

    @PutMapping("/{gameId}/buy")
    public void processBuyProposal(@PathVariable("gameId") UUID gameId,
                                   @RequestParam("action") ProposalAction action) {
        gameService.processBuyProposal(gameId, action);
    }

    @PutMapping("/{gameId}/pay")
    public void processPayment(@PathVariable("gameId") UUID gameId) {
        gameService.processPayment(gameId);
    }

    @PutMapping("/{gameId}/auction/buy")
    public void processAuctionBuyProposal(@PathVariable("gameId") UUID gameId,
                                          @RequestParam("action") ProposalAction action) {
        gameService.processAuctionBuyProposal(gameId, action);
    }

    @PutMapping("/{gameId}/auction/raise")
    public void processAuctionRaiseProposal(@PathVariable("gameId") UUID gameId,
                                            @RequestParam("action") ProposalAction action) {
        gameService.processAuctionRaiseProposal(gameId, action);
    }

    @PutMapping("/{gameId}/jail")
    public void processJailAction(@PathVariable("gameId") UUID gameId,
                                  @RequestParam("action") JailAction jailAction) {
        gameService.processJailAction(gameId, jailAction);
    }

    @PutMapping("/{gameId}/give_up")
    public void giveUp(@PathVariable("gameId") UUID gameId,
                       @RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.giveUp(gameId, playerId);
    }
}