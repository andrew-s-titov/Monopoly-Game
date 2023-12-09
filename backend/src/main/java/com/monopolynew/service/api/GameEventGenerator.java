package com.monopolynew.service.api;

import com.monopolynew.dto.CheckToPay;
import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.GameRoomEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.state.Auction;
import com.monopolynew.game.state.BuyProposal;

public interface GameEventGenerator {

    GameMapRefreshEvent mapRefreshEvent(Game game);

    OfferProposalEvent offerProposalEvent(Game game);

    AuctionBuyProposalEvent auctionBuyProposalEvent(Auction auction);

    AuctionRaiseProposalEvent auctionRaiseProposalEvent(Auction auction);

    BuyProposalEvent buyProposalEvent(BuyProposal buyProposal);

    PayCommandEvent payCommandEvent(CheckToPay checkToPay);

    GameRoomEvent gameRoomEvent(Game game);

    DiceResultEvent diceResultEvent(Game game);
}