package com.monopolynew.service.impl;

import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.AuctionBuyProposalEvent;
import com.monopolynew.event.AuctionRaiseProposalEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.Auction;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class AuctionManagerImpl implements AuctionManager {

    private final GameHelper gameHelper;
    private final GameEventSender gameEventSender;

    @Override
    public void startNewAuction(Game game, PurchasableField field) {
        var currentPlayer = game.getCurrentPlayer();
        game.setAuction(new Auction(game, field));
        game.setStage(GameStage.AUCTION);
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(currentPlayer.getName() + " started an auction"));
        auctionStep(game);
    }

    @Override
    public void auctionStep(Game game) {
        var auction = game.getAuction();
        List<Player> participants = auction.getParticipants();
        if (participants.size() == 0) {
            // there's no one who wanted to or could take part in the auction
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text("No one took part in the auction"));
            finishAuction(game);
        } else if (participants.size() > 1) {
            // propose to raise the stake
            var player = auction.getNextPlayer();
            if (player.getMoney() >= auction.getAuctionPrice() + Rules.AUCTION_STEP) {
                gameEventSender.sendToPlayer(player.getId(),
                        AuctionRaiseProposalEvent.propose(player, auction.getField().getName(), auction.getAuctionPrice()));
            } else {
                // player can't afford to raise the stake - exclude from participants and proceed to next player
                auction.removeParticipant();
                auctionStep(game); // TODO: check for logic and bugs
            }
        } else {
            var player = auction.getNextPlayer();
            if (auction.isFirstCircle()) {
                // if first circle - next player is the only participant: propose on start price
                var playerId = player.getId();
                gameEventSender.sendToPlayer(playerId,
                        new AuctionBuyProposalEvent(playerId, auction.getField().getName(), auction.getAuctionPrice()));
            } else {
                // automatically sell to the winner
                gameHelper.doBuyField(game, auction.getField(), auction.getAuctionPrice(), player);
                finishAuction(game);
            }
        }
    }

    @Override
    public void processAuctionBuyProposal(Game game, ProposalAction action) {
        var auction = game.getAuction();
        checkAuctionAvailability(game);
        Assert.notNull(action, NULL_ARG_MESSAGE);
        if (ProposalAction.ACCEPT.equals(action)) {
            Player buyer = auction.getCurrentParticipant();
            gameHelper.doBuyField(game, auction.getField(), auction.getAuctionPrice(), buyer);
        } else {
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text("No one took part in the auction"));
        }
        finishAuction(game);
    }

    @Override
    public void processAuctionRaiseProposal(Game game, ProposalAction action) {
        Auction auction = game.getAuction();
        checkAuctionAvailability(game);
        Assert.notNull(action, NULL_ARG_MESSAGE);
        if (ProposalAction.ACCEPT.equals(action)) {
            auction.raiseTheStake();
            gameEventSender.sendToAllPlayers(
                    SystemMessageEvent.text("The lot price went up to $" + auction.getAuctionPrice() + "k"));
        } else if (ProposalAction.DECLINE.equals(action)) {
            auction.removeParticipant();
        }
        auctionStep(game);
    }

    private void finishAuction(Game game) {
        game.setStage(GameStage.TURN_START);
        game.setAuction(null);
        gameHelper.endTurn(game);
    }

    private void checkAuctionAvailability(Game game) {
        if (!GameStage.AUCTION.equals(game.getStage())) {
            throw new IllegalStateException("can't process auction - wrong game stage");
        }
        if (game.getAuction() == null) {
            throw new IllegalStateException("no active auction found");
        }
    }
}
