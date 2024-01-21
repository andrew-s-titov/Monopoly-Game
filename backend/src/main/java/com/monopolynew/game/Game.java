package com.monopolynew.game;

import com.monopolynew.enums.GameStage;
import com.monopolynew.game.procedure.Auction;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.game.procedure.CheckToPay;
import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.game.procedure.Offer;
import com.monopolynew.map.GameMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.monopolynew.util.CommonUtils.requireNotNullArgs;

@RequiredArgsConstructor
public class Game {

    @Getter
    private final UUID id = UUID.randomUUID();
    private final Map<UUID, Player> players = new HashMap<>();

    private final boolean withTeleport;

    @Getter
    private boolean inProgress = false;
    @Getter
    @Setter
    private GameStage stage;
    @Getter
    @Setter
    private DiceResult lastDice;
    @Getter
    @Setter
    private Auction auction;
    @Getter
    @Setter
    private BuyProposal buyProposal;
    @Getter
    @Setter
    private CheckToPay checkToPay;
    @Getter
    @Setter
    private Offer offer;
    private Iterator<Player> playerIterator;
    @Getter
    private GameMap gameMap;
    private UUID whoseTurn;

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public Player getPlayerById(@NonNull UUID playerId) {
        requireNotNullArgs(playerId);
        return this.players.get(playerId);
    }

    public boolean playerExists(UUID playerId) {
        return this.players.containsKey(playerId);
    }

    public void addPlayer(Player player) {
        this.players.put(player.getId(), player);
    }

    public void removePlayer(UUID playerId) {
        this.players.remove(playerId);
    }

    public void startGame() {
        this.inProgress = true;
        this.stage = GameStage.TURN_START;
        this.gameMap = new GameMap(this.withTeleport);
        this.whoseTurn = nextPlayer().getId();
    }

    public Player getCurrentPlayer() {
        return this.players.get(this.whoseTurn);
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