package com.monopolynew.service.api;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.game.Game;

import java.util.UUID;

public interface DealManager {

    void createOffer(Game game, UUID offerInitiatorId, UUID offerAddresseeId, DealOffer offer);

    void processOfferAnswer(Game game, UUID callerId, ProposalAction proposalAction);
}