package com.monopolynew.service;

import com.monopolynew.game.Game;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameRepository {

    private final Game game = new Game(false);

    public Game getGame() {
        // TODO get game by its ID
        return this.game;
    }

    public UUID createGame(int maxPlayers, boolean withTeleport) {
        // TODO: implement for multi-game env
        return UUID.randomUUID();
    }

    public void removeGame(UUID gameId) {
        // TODO: implement for multi-game env
    }
}