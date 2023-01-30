package com.monopolynew.service;

import com.monopolynew.dto.CheckToPay;
import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.state.Auction;
import com.monopolynew.game.state.BuyProposal;

public interface GameEventGenerator {

    GameMapRefreshEvent newMapRefreshEvent(Game game);

    OfferProposalEvent newOfferProposalEvent(Game game);

    AuctionBuyProposalEvent newAuctionBuyProposalEvent(Auction auction);

    AuctionRaiseProposalEvent newAuctionRaiseProposalEvent(Auction auction);

    BuyProposalEvent newBuyProposalEvent(BuyProposal buyProposal);

    PayCommandEvent newPayCommandEvent(CheckToPay checkToPay);
}