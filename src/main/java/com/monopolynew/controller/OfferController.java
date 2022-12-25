package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.PreDealInfo;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game/offer")
public class OfferController {

    private final GameService gameService;

    @GetMapping("/{offerAddresseeId}/info")
    public PreDealInfo preDealInfo(@CookieValue(GlobalConfig.PLAYER_ID_KEY) String offerInitiatorId,
                                   @PathVariable("offerAddresseeId") String offerAddresseeId) {
        return gameService.getPreDealInfo(offerInitiatorId, offerAddresseeId);
    }

    @PostMapping("/{offerAddresseeId}/send")
    public void sendOffer(@CookieValue(GlobalConfig.PLAYER_ID_KEY) String offerInitiatorId,
                          @PathVariable("offerAddresseeId") String offerAddresseeId,
                          @RequestBody DealOffer dealOffer) {
        gameService.createOffer(offerInitiatorId, offerAddresseeId, dealOffer);
    }

    @PostMapping("/process")
    public void processOffer(@CookieValue(GlobalConfig.PLAYER_ID_KEY) String callerId,
                             @RequestParam("action") ProposalAction proposalAction) {
        gameService.processOfferAnswer(callerId, proposalAction);
    }
}