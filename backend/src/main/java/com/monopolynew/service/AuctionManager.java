package com.monopolynew.service;

import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.Auction;
import com.monopolynew.map.PurchasableField;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.monopolynew.util.CommonUtils.requireNotNullArgs;

@RequiredArgsConstructor
@Component
public class AuctionManager {

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;

    public void startNewAuction(Game game, PurchasableField field) {
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        var currentPlayer = game.getCurrentPlayer();
        game.setAuction(new Auction(game, field));
        gameEventSender.sendToAllPlayers(game.getId(),
                new SystemMessageEvent("event.auction.start", Map.of(
                        "name", currentPlayer.getName())));
        auctionStep(game);
    }

    public void auctionStep(Game game) {
        var gameId = game.getId();
        var auction = game.getAuction();
        List<Player> participants = auction.getParticipants();
        if (participants.isEmpty()) {
            // there's no one who wanted to or could take part in the auction
            gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.auction.noParticipants"));
            finishAuction(game);
        } else if (participants.size() > 1) {
            // propose to raise the stake
            var player = auction.getNextPlayer();
            if (player.getMoney() >= auction.getAuctionPrice() + Rules.AUCTION_STEP) {
                gameLogicExecutor.changeGameStage(game, GameStage.AWAITING_AUCTION_RAISE);
                gameEventSender.sendToPlayer(gameId, player.getId(), gameEventGenerator.auctionRaiseProposalEvent(auction));
            } else {
                // player can't afford to raise the stake - exclude from participants and proceed to next player
                auction.removeParticipant();
                auctionStep(game);
            }
        } else {
            var playerId = auction.getNextPlayer().getId();
            if (auction.isFirstCircle()) {
                // if first circle - next playerId is the only participant: propose for start price
                gameLogicExecutor.changeGameStage(game, GameStage.AWAITING_AUCTION_BUY);
                gameEventSender.sendToPlayer(gameId, playerId, gameEventGenerator.auctionBuyProposalEvent(auction));
            } else {
                // automatically sell to the winner
                gameLogicExecutor.doBuyField(game, auction.getField(), auction.getAuctionPrice(), playerId);
                finishAuction(game);
            }
        }
    }

    public void processAuctionBuyProposal(Game game, @NonNull ProposalAction action) {
        requireNotNullArgs(game, action);
        var gameId = game.getId();
        var auction = game.getAuction();
        checkAuctionAvailability(game, GameStage.AWAITING_AUCTION_BUY, auction);
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        if (ProposalAction.ACCEPT.equals(action)) {
            var buyerId = auction.getCurrentParticipant().getId();
            gameLogicExecutor.doBuyField(game, auction.getField(), auction.getAuctionPrice(), buyerId);
        } else {
            gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.auction.noParticipants"));
        }
        finishAuction(game);
    }

    public void processAuctionRaiseProposal(Game game, @NonNull ProposalAction action) {
        requireNotNullArgs(game, action);
        var gameId = game.getId();
        Auction auction = game.getAuction();
        checkAuctionAvailability(game, GameStage.AWAITING_AUCTION_RAISE, auction);
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        if (ProposalAction.ACCEPT.equals(action)) {
            auction.raiseTheStake();
            gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent(
                    "event.auction.raise", Map.of(
                            "name", auction.getCurrentParticipant().getName(),
                            "price", auction.getAuctionPrice())));
        } else if (ProposalAction.DECLINE.equals(action)) {
            auction.removeParticipant();
        }
        auctionStep(game);
    }

    private void finishAuction(Game game) {
        game.setAuction(null);
        gameLogicExecutor.endTurn(game);
    }

    private void checkAuctionAvailability(Game game, GameStage expectedGameStage, Auction auction) {
        if (!expectedGameStage.equals(game.getStage())) {
            throw new WrongGameStageException("can't process auction - wrong game stage");
        }
        if (auction == null) {
            throw new IllegalStateException("no active auction found");
        }
    }
}
