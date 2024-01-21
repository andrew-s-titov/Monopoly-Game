package com.monopolynew.service;

import com.monopolynew.enums.GameStage;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.PayCommandEvent;
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
        var gameId = game.getId();
        var currentPlayerId = game.getCurrentPlayer().getId();
        gameEventSender.sendToPlayer(gameId, playerId, gameEventGenerator.mapStateEvent(game));
        gameEventSender.sendToPlayer(gameId, playerId, gameEventGenerator.gameRoomEvent(game));

        var gameStage = game.getStage();
        if (currentPlayerId.equals(playerId)) {
            switch (gameStage) {
                case TURN_START: {
                    gameEventSender.sendToPlayer(gameId, playerId, new TurnStartEvent());
                    break;
                }
                case JAIL_RELEASE_START: {
                    gameEventSender.sendToPlayer(gameId, playerId, new JailReleaseProcessEvent());
                    break;
                }
                case BUY_PROPOSAL: {
                    gameEventSender.sendToPlayer(gameId, playerId, gameEventGenerator.buyProposalEvent(game.getBuyProposal()));
                    break;
                }
                case AWAITING_PAYMENT, AWAITING_JAIL_FINE: {
                    gameEventSender.sendToPlayer(gameId, playerId, PayCommandEvent.of(game.getCheckToPay()));
                    break;
                }
                case ROLLED_FOR_JAIL, ROLLED_FOR_TURN: {
                    gameEventSender.sendToAllPlayers(gameId, DiceResultEvent.of(game.getLastDice()));
                    break;
                }
                default:
                    break;
            }
        } else {
            if (GameStage.DEAL_OFFER.equals(gameStage) && game.getOffer().getAddressee().getId().equals(playerId)) {
                gameEventSender.sendToPlayer(gameId, playerId, gameEventGenerator.offerProposalEvent(game));
            }
        }

        if (GameStage.AWAITING_AUCTION_BUY.equals(gameStage) || GameStage.AWAITING_AUCTION_RAISE.equals(gameStage)) {
            var auction = game.getAuction();
            var currentParticipantId = auction.getCurrentParticipant().getId();
            if (playerId.equals(currentParticipantId)) {
                gameEventSender.sendToPlayer(gameId, playerId, GameStage.AWAITING_AUCTION_BUY.equals(gameStage) ?
                        gameEventGenerator.auctionBuyProposalEvent(auction) :
                        gameEventGenerator.auctionRaiseProposalEvent(auction));
            }
        }
    }
}
