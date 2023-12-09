package com.monopolynew.service;

import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.Auction;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.api.AuctionManager;
import com.monopolynew.service.api.GameEventGenerator;
import com.monopolynew.service.api.GameEventSender;
import com.monopolynew.service.api.GameLogicExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class AuctionManagerImpl implements AuctionManager {

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;

    @Override
    public void startNewAuction(Game game, PurchasableField field) {
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        var currentPlayer = game.getCurrentPlayer();
        game.setAuction(new Auction(game, field));
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(currentPlayer.getName() + " started an auction"));
        auctionStep(game);
    }

    @Override
    public void auctionStep(Game game) {
        var auction = game.getAuction();
        List<Player> participants = auction.getParticipants();
        if (participants.isEmpty()) {
            // there's no one who wanted to or could take part in the auction
            gameEventSender.sendToAllPlayers(new ChatMessageEvent("No one took part in the auction"));
            finishAuction(game);
        } else if (participants.size() > 1) {
            // propose to raise the stake
            var player = auction.getNextPlayer();
            if (player.getMoney() >= auction.getAuctionPrice() + Rules.AUCTION_STEP) {
                gameLogicExecutor.changeGameStage(game, GameStage.AWAITING_AUCTION_RAISE);
                gameEventSender.sendToAllPlayers(gameEventGenerator.auctionRaiseProposalEvent(auction));
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
                gameEventSender.sendToAllPlayers(gameEventGenerator.auctionBuyProposalEvent(auction));
            } else {
                // automatically sell to the winner
                gameLogicExecutor.doBuyField(game, auction.getField(), auction.getAuctionPrice(), playerId);
                finishAuction(game);
            }
        }
    }

    @Override
    public void processAuctionBuyProposal(Game game, @NonNull ProposalAction action) {
        Assert.notNull(action, NULL_ARG_MESSAGE);
        var auction = game.getAuction();
        checkAuctionAvailability(game, GameStage.AWAITING_AUCTION_BUY, auction);
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        if (ProposalAction.ACCEPT.equals(action)) {
            var buyerId = auction.getCurrentParticipant().getId();
            gameLogicExecutor.doBuyField(game, auction.getField(), auction.getAuctionPrice(), buyerId);
        } else {
            gameEventSender.sendToAllPlayers(new ChatMessageEvent("No one took part in the auction"));
        }
        finishAuction(game);
    }

    @Override
    public void processAuctionRaiseProposal(Game game, @NonNull ProposalAction action) {
        Assert.notNull(action, NULL_ARG_MESSAGE);
        Auction auction = game.getAuction();
        checkAuctionAvailability(game, GameStage.AWAITING_AUCTION_RAISE, auction);
        gameLogicExecutor.changeGameStage(game, GameStage.AUCTION_IN_PROGRESS);
        if (ProposalAction.ACCEPT.equals(action)) {
            auction.raiseTheStake();
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                    String.format("%s raised lot price to $%s",
                            auction.getCurrentParticipant().getName(),
                            auction.getAuctionPrice())));
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
