package com.monopolynew.service;

import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.GameRoomEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.Auction;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.game.procedure.CheckToPay;
import com.monopolynew.game.procedure.DiceResult;
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

    public GameMapRefreshEvent mapRefreshEvent(Game game) {
        var purchasableFields = game.getGameMap().getFields().stream()
                .filter(PurchasableField.class::isInstance)
                .map(PurchasableField.class::cast)
                .toList();
        var gameFieldViews = gameFieldMapper.toStateList(purchasableFields);
        return new GameMapRefreshEvent(game.getPlayers(), gameFieldViews, game.getCurrentPlayer().getId());
    }

    public OfferProposalEvent offerProposalEvent(Game game) {
        var offer = game.getOffer();

        var currentPlayer = game.getCurrentPlayer();
        return OfferProposalEvent.builder()
                .initiatorName(currentPlayer.getName())
                .addresseeId(offer.getAddressee().getId())
                .addresseeFields(offer.getAddresseeFields().stream().map(GameField::getId).toList())
                .initiatorFields(offer.getInitiatorFields().stream().map(GameField::getId).toList())
                .addresseeMoney(offer.getAddresseeMoney())
                .initiatorMoney(offer.getInitiatorMoney())
                .build();
    }

    public AuctionBuyProposalEvent auctionBuyProposalEvent(Auction auction) {
        return new AuctionBuyProposalEvent(
                auction.getCurrentParticipant().getId(),
                auction.getField().getId(),
                auction.getAuctionPrice()
        );
    }

    public AuctionRaiseProposalEvent auctionRaiseProposalEvent(Auction auction) {
        return new AuctionRaiseProposalEvent(
                auction.getCurrentParticipant().getId(),
                auction.getField().getId(),
                auction.getAuctionPrice() + Rules.AUCTION_STEP
        );
    }

    public BuyProposalEvent buyProposalEvent(BuyProposal buyProposal) {
        var purchasableField = buyProposal.getField();
        return BuyProposalEvent.builder()
                .playerId(buyProposal.getPlayerId())
                .price(purchasableField.getPrice())
                .payable(buyProposal.isPayable())
                .fieldIndex(purchasableField.getId())
                .build();
    }

    public PayCommandEvent payCommandEvent(CheckToPay checkToPay) {
        var debtor = checkToPay.getDebtor();
        var debt = checkToPay.getDebt();
        return new PayCommandEvent(debtor.getId(), debt, debtor.getMoney() >= debt, checkToPay.isWiseToGiveUp());
    }

    public GameRoomEvent gameRoomEvent(Game game) {
        return new GameRoomEvent(playerMapper.toUserInfoList(game.getPlayers()));
    }

    public DiceResultEvent diceResultEvent(Game game) {
        DiceResult lastDice = game.getLastDice();
        return new DiceResultEvent(game.getCurrentPlayer().getId(),
                lastDice.getFirstDice(), lastDice.getSecondDice());
    }
}