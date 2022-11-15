package com.monopolynew.service;

import com.monopolynew.enums.ProposalAction;
import com.monopolynew.game.Game;
import com.monopolynew.map.PurchasableField;

public interface AuctionManager {

    void startNewAuction(Game game, PurchasableField field);

    void auctionStep(Game game);

    void processAuctionBuyProposal(Game game, ProposalAction action);

    void processAuctionRaiseProposal(Game game, ProposalAction action);
}
