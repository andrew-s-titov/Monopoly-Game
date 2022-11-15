package com.monopolynew.game;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.game.state.Auction;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class Game {

    private final boolean withTeleport;

    private final Map<String, Player> players = new HashMap<>();

    private boolean gameInProgress = false;
    @Getter @Setter
    private GameStage stage;
    @Getter @Setter
    private DiceResult lastDice;
    @Getter @Setter
    private Auction auction;
    @Getter @Setter
    private BuyProposal buyProposal;
    private Iterator<Player> playerIterator;
    private GameMap gameMap;
    private String whoseTurn;


    public GameMap getCurrentMap() {
        return this.gameMap;
    }

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public Player getPlayer(@NonNull String playerId) {
        Assert.notNull(playerId, "Cannot get player with 'null' id");
        return this.players.get(playerId);
    }

    public boolean playerExists(String playerId) {
        return this.players.containsKey(playerId);
    }

    public boolean usernameTaken(String username) {
        return this.players.values().stream()
                .anyMatch(player -> player.getName().equalsIgnoreCase(username.strip()));
    }

    public void addPlayer(Player player) {
        this.players.put(player.getId(), player);
    }

    public void disconnectPlayer(String playerId) {
        this.players.remove(playerId);
    }

    public boolean isGameInProgress() {
        return this.gameInProgress;
    }

    public void startGame() {
        this.gameInProgress = true;
        this.stage = GameStage.TURN_START;
        this.gameMap = new GameMap(this.withTeleport);
        this.whoseTurn = nextPlayer().getId();
    }

    public void finishGame() {
        this.players.clear();
        this.gameMap = null;
        this.whoseTurn = null;
        this.playerIterator = null;
        this.gameInProgress = false;
    }

    public Player getCurrentPlayer() {
        return this.players.get(this.whoseTurn);
    }

    public GameMapRefreshEvent mapRefreshEvent() {
        return new GameMapRefreshEvent(this.players.values(), this.gameMap.getFields(), this.whoseTurn);
    }

    public Player nextPlayer() {
        if (this.playerIterator == null || !this.playerIterator.hasNext()) {
            this.playerIterator = players.values().iterator();
        }
        Player next = this.playerIterator.next();
        this.whoseTurn = next.getId();
        return next;
    }
}