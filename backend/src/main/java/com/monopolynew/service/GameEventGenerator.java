package com.monopolynew.service;

import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.GameMapStateEvent;
import com.monopolynew.event.GameRoomEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.Auction;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.mapper.GameFieldMapper;
import com.monopolynew.mapper.PlayerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameEventGenerator {

    private final GameFieldMapper gameFieldMapper;
    private final PlayerMapper playerMapper;

    public GameMapStateEvent mapRefreshEvent(Game game) {
        var purchasableFields = game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .toList();
        var gameFieldViews = gameFieldMapper.toStateList(purchasableFields);
        return new GameMapStateEvent(game.getPlayers(), gameFieldViews, game.getCurrentPlayer().getId());
    }

    public OfferProposalEvent offerProposalEvent(Game game) {
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

    public AuctionBuyProposalEvent auctionBuyProposalEvent(Auction auction) {
        return new AuctionBuyProposalEvent(
                auction.getField().getId(),
                auction.getAuctionPrice()
        );
    }

    public AuctionRaiseProposalEvent auctionRaiseProposalEvent(Auction auction) {
        return new AuctionRaiseProposalEvent(
                auction.getField().getId(),
                auction.getAuctionPrice() + Rules.AUCTION_STEP
        );
    }

    public BuyProposalEvent buyProposalEvent(BuyProposal buyProposal) {
        var purchasableField = buyProposal.getField();
        return BuyProposalEvent.builder()
                .price(purchasableField.getPrice())
                .fieldIndex(purchasableField.getId())
                .build();
    }

    public GameRoomEvent gameRoomEvent(Game game) {
        return new GameRoomEvent(playerMapper.toUserInfoList(game.getPlayers()));
    }
}