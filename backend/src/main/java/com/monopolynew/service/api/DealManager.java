package com.monopolynew.service.api;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.game.Game;

public interface DealManager {

    void createOffer(Game game, String offerInitiatorId, String offerAddresseeId, DealOffer offer);

    void processOfferAnswer(Game game, String callerId, ProposalAction proposalAction);
}