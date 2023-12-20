package com.monopolynew.service;

import com.monopolynew.enums.GameStage;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GameMapRefresher {

    private final GameEventGenerator gameEventGenerator;
    private final GameEventSender gameEventSender;

    public void restoreGameStateForPlayer(Game game, UUID playerId) {
        var currentPlayerId = game.getCurrentPlayer().getId();
        gameEventSender.sendToPlayer(playerId, gameEventGenerator.mapRefreshEvent(game));
        gameEventSender.sendToPlayer(playerId, gameEventGenerator.gameRoomEvent(game));

        var gameStage = game.getStage();
        if (currentPlayerId.equals(playerId)) {
            switch (gameStage) {
                case TURN_START: {
                    gameEventSender.sendToPlayer(playerId, new TurnStartEvent(currentPlayerId));
                    break;
                }
                case JAIL_RELEASE_START: {
                    gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(playerId));
                    break;
                }
                case BUY_PROPOSAL: {
                    gameEventSender.sendToPlayer(playerId, gameEventGenerator.buyProposalEvent(game.getBuyProposal()));
                    break;
                }
                case AWAITING_PAYMENT, AWAITING_JAIL_FINE: {
                    gameEventSender.sendToPlayer(playerId, gameEventGenerator.payCommandEvent(game.getCheckToPay()));
                    break;
                }
                case ROLLED_FOR_JAIL, ROLLED_FOR_TURN: {
                    gameEventSender.sendToAllPlayers(gameEventGenerator.diceResultEvent(game));
                    break;
                }
                default:
                    break;
            }
        } else {
            if (GameStage.DEAL_OFFER.equals(gameStage) && game.getOffer().getAddressee().getId().equals(playerId)) {
                gameEventSender.sendToPlayer(playerId, gameEventGenerator.offerProposalEvent(game));
            }
        }

        if (GameStage.AWAITING_AUCTION_BUY.equals(gameStage) || GameStage.AWAITING_AUCTION_RAISE.equals(gameStage)) {
            var auction = game.getAuction();
            var currentParticipantId = auction.getCurrentParticipant().getId();
            if (playerId.equals(currentParticipantId)) {
                gameEventSender.sendToPlayer(playerId, GameStage.AWAITING_AUCTION_BUY.equals(gameStage) ?
                        gameEventGenerator.auctionBuyProposalEvent(auction) :
                        gameEventGenerator.auctionRaiseProposalEvent(auction));
            }
        }
    }
}
