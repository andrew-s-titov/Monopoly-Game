package com.monopolynew.service;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.PreDealInfo;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.game.Game;

public interface DealManager {

    PreDealInfo getPreDealInfo(Game game, String offerInitiatorId, String offerAddresseeId);

    void createOffer(Game game, String offerInitiatorId, String offerAddresseeId, DealOffer offer);

    void processOfferAnswer(Game game, String callerId, ProposalAction proposalAction);
}