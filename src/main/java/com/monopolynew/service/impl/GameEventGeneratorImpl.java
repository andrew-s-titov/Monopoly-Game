package com.monopolynew.service.impl;

import com.monopolynew.dto.CheckToPay;
import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.Auction;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameFieldConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameEventGeneratorImpl implements GameEventGenerator {

    private final GameFieldConverter gameFieldConverter;

    @Override
    public GameMapRefreshEvent newMapRefreshEvent(Game game) {
        var gameFieldViews = gameFieldConverter.toListView(game.getGameMap().getFields());
        return new GameMapRefreshEvent(game.getPlayers(), gameFieldViews, game.getCurrentPlayer().getId());
    }

    @Override
    public OfferProposalEvent newOfferProposalEvent(Game game) {
        var offer = game.getOffer();
        var moneyToGive = offer.getMoneyToGive();
        var moneyToReceive = offer.getMoneyToReceive();

        var currentPlayer = game.getCurrentPlayer();
        return OfferProposalEvent.builder()
                .initiatorName(currentPlayer.getName())
                .fieldsToBuy(gameFieldConverter.toListOfferView(offer.getFieldsToBuy()))
                .fieldsToSell(gameFieldConverter.toListOfferView(offer.getFieldsToSell()))
                .moneyToGive(moneyToGive)
                .moneyToReceive(moneyToReceive)
                .build();
    }

    @Override
    public AuctionBuyProposalEvent newAuctionBuyProposalEvent(Auction auction) {
        return new AuctionBuyProposalEvent(
                auction.getField().getName(),
                auction.getAuctionPrice()
        );
    }

    @Override
    public AuctionRaiseProposalEvent newAuctionRaiseProposalEvent(Auction auction) {
        return new AuctionRaiseProposalEvent(
                auction.getCurrentParticipant().getId(),
                auction.getField().getName(),
                auction.getAuctionPrice() + Rules.AUCTION_STEP
        );
    }

    @Override
    public BuyProposalEvent newBuyProposalEvent(BuyProposal buyProposal) {
        var purchasableField = buyProposal.getField();
        return new BuyProposalEvent(buyProposal.getPlayerId(), purchasableField.getName(), purchasableField.getPrice(), buyProposal.isPayable());
    }

    @Override
    public PayCommandEvent newPayCommandEvent(CheckToPay checkToPay) {
        var player = checkToPay.getPlayer();
        var checkSum = checkToPay.getSum();
        return new PayCommandEvent(player.getId(), checkSum, player.getMoney() >= checkSum, checkToPay.isWiseToGiveUp());
    }
}