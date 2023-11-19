package com.monopolynew.service.impl;

import com.monopolynew.enums.GameStage;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.OfferSentEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Rules;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameMapRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameMapRefresherImpl implements GameMapRefresher {

    private final GameEventGeneratorImpl gameEventGenerator;
    private final GameEventSender gameEventSender;

    @Override
    public void restoreGameStateForPlayer(Game game, String playerId) {
        var player = game.getPlayerById(playerId);
        var currentPlayerId = game.getCurrentPlayer().getId();
        gameEventSender.sendToPlayer(playerId, gameEventGenerator.newMapRefreshEvent(game));

        var gameStage = game.getStage();
        if (currentPlayerId.equals(playerId)) {
            switch (gameStage) {
                case TURN_START: {
                    gameEventSender.sendToPlayer(playerId, new TurnStartEvent(currentPlayerId));
                    break;
                }
                case JAIL_RELEASE_START: {
                    gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(
                            playerId, player.getMoney() >= Rules.JAIL_BAIL));
                    break;
                }
                case BUY_PROPOSAL: {
                    gameEventSender.sendToPlayer(playerId, gameEventGenerator.newBuyProposalEvent(game.getBuyProposal()));
                    break;
                }
                case AWAITING_AUCTION_BUY: {
                    var auction = game.getAuction();
                    if (playerId.equals(auction.getCurrentParticipant().getId())) {
                        gameEventSender.sendToPlayer(playerId, gameEventGenerator.newAuctionBuyProposalEvent(auction));
                    }
                    break;
                }
                case AWAITING_AUCTION_RAISE: {
                    var auction = game.getAuction();
                    if (playerId.equals(auction.getCurrentParticipant().getId())) {
                        gameEventSender.sendToPlayer(playerId, gameEventGenerator.newAuctionRaiseProposalEvent(auction));
                    }
                    break;
                }
                case AWAITING_PAYMENT, AWAITING_JAIL_FINE: {
                    gameEventSender.sendToPlayer(playerId, gameEventGenerator.newPayCommandEvent(game.getCheckToPay()));
                    break;
                }
                case ROLLED_FOR_JAIL, ROLLED_FOR_TURN: {
                    var lastDice = game.getLastDice();
                    gameEventSender.sendToAllPlayers(
                            new DiceResultEvent(playerId, lastDice.getFirstDice(), lastDice.getSecondDice()));
                    break;
                }
                case DEAL_OFFER: {
                    gameEventSender.sendToPlayer(currentPlayerId, new OfferSentEvent());
                    break;
                }
                default: break;
            }
        } else {
            if (GameStage.DEAL_OFFER.equals(gameStage)) {
                if (game.getOffer().getAddressee().getId().equals(playerId)) {
                    gameEventSender.sendToPlayer(playerId, gameEventGenerator.newOfferProposalEvent(game));
                }
            }
        }

        if (GameStage.AWAITING_AUCTION_BUY.equals(gameStage) || GameStage.AWAITING_AUCTION_RAISE.equals(gameStage)) {
            var auction = game.getAuction();
            var currentParticipantId = auction.getCurrentParticipant().getId();
            if (playerId.equals(currentParticipantId)) {
                gameEventSender.sendToPlayer(playerId, GameStage.AWAITING_AUCTION_BUY.equals(gameStage) ?
                        gameEventGenerator.newAuctionBuyProposalEvent(auction) :
                        gameEventGenerator.newAuctionRaiseProposalEvent(auction));
            }
        }
    }
}