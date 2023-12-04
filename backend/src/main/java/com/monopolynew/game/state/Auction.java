package com.monopolynew.game.state;

import com.monopolynew.game.Game;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import lombok.Getter;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Auction {
    @Getter
    private final PurchasableField field;
    @Getter
    private final List<Player> participants;
    @Getter
    private int auctionPrice;
    private Player currentParticipant;

    private Iterator<Player> playerIterator;
    private int auctionCircle = 1;

    public Auction(Game game, PurchasableField field) {
        this.field = field;
        this.auctionPrice = field.getPrice();
        Player auctionInitiator = game.getCurrentPlayer();
        this.participants = game.getPlayers().stream()
                .filter(player -> !player.equals(auctionInitiator))
                .filter(player -> !player.isBankrupt())
                .filter(player -> player.getMoney() >= auctionPrice)
                .collect(Collectors.toList());
        this.playerIterator = participants.iterator();
    }

    public void raiseTheStake() {
        auctionPrice += Rules.AUCTION_STEP;
    }

    public Player getNextPlayer() {
        if (!playerIterator.hasNext()) {
            playerIterator = participants.iterator();
            auctionCircle++;
        }
        currentParticipant = playerIterator.next();
        if (currentParticipant.isBankrupt()) {
            playerIterator.remove();
            currentParticipant = getNextPlayer();
        }
        return currentParticipant;
    }

    public Player getCurrentParticipant() {
        return this.currentParticipant;
    }

    public boolean isFirstCircle() {
        return auctionCircle == 1;
    }

    public void removeParticipant() {
        playerIterator.remove();
    }
}