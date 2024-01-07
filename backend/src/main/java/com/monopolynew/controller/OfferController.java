package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.DealOffer;
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
@RequestMapping("/game/offer")
public class OfferController {

    private final GameService gameService;

    @PostMapping("/{addresseeId}")
    public void sendOffer(@RequestHeader(GlobalConfig.USER_ID_HEADER) UUID initiatorId,
                          @PathVariable("addresseeId") UUID addresseeId,
                          @RequestBody DealOffer dealOffer) {
        gameService.createOffer(initiatorId, addresseeId, dealOffer);
    }

    @PutMapping
    public void processOffer(@RequestHeader(GlobalConfig.USER_ID_HEADER) UUID callerId,
                             @RequestParam("action") ProposalAction proposalAction) {
        gameService.processOfferAnswer(callerId, proposalAction);
    }
}
