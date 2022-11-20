package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.service.GameHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameHolderImpl implements GameHolder {

    private final Game game = new Game(false);

    @Override
    public Game getGame() {
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