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
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
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
        var purchasableFields = game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .toList();
        var gameFieldViews = gameFieldConverter.toListView(purchasableFields);
        return new GameMapRefreshEvent(game.getPlayers(), gameFieldViews, game.getCurrentPlayer().getId());
    }

    @Override
    public OfferProposalEvent newOfferProposalEvent(Game game) {
        var offer = game.getOffer();

        var currentPlayer = game.getCurrentPlayer();
        return OfferProposalEvent.builder()
                .initiatorName(currentPlayer.getName())
                .addresseeFields(offer.getAddresseeFields().stream().map(GameField::getId).toList())
                .initiatorFields(offer.getInitiatorFields().stream().map(GameField::getId).toList())
                .addresseeMoney(offer.getAddresseeMoney())
                .initiatorMoney(offer.getInitiatorMoney())
                .build();
    }

    @Override
    public AuctionBuyProposalEvent newAuctionBuyProposalEvent(Auction auction) {
        return new AuctionBuyProposalEvent(
                auction.getField().getId(),
                auction.getAuctionPrice()
        );
    }

    @Override
    public AuctionRaiseProposalEvent newAuctionRaiseProposalEvent(Auction auction) {
        return new AuctionRaiseProposalEvent(
                auction.getCurrentParticipant().getId(),
                auction.getField().getId(),
                auction.getAuctionPrice() + Rules.AUCTION_STEP
        );
    }

    @Override
    public BuyProposalEvent newBuyProposalEvent(BuyProposal buyProposal) {
        var purchasableField = buyProposal.getField();
        return BuyProposalEvent.builder()
                .playerId(buyProposal.getPlayerId())
                .price(purchasableField.getPrice())
                .payable(buyProposal.isPayable())
                .fieldIndex(purchasableField.getId())
                .build();
    }

    @Override
    public PayCommandEvent newPayCommandEvent(CheckToPay checkToPay) {
        var player = checkToPay.getPlayer();
        var checkSum = checkToPay.getSum();
        return new PayCommandEvent(player.getId(), checkSum, player.getMoney() >= checkSum, checkToPay.isWiseToGiveUp());
    }
}