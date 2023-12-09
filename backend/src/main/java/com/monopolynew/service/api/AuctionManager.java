package com.monopolynew.service.api;

import com.monopolynew.enums.ProposalAction;
import com.monopolynew.game.Game;
import com.monopolynew.map.PurchasableField;
import org.springframework.lang.NonNull;

public interface AuctionManager {

    void startNewAuction(Game game, PurchasableField field);

    void auctionStep(Game game);

    void processAuctionBuyProposal(Game game, @NonNull ProposalAction action);

    void processAuctionRaiseProposal(Game game, @NonNull ProposalAction action);
}
