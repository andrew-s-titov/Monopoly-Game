package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.service.GameHolder;
import org.springframework.stereotype.Component;

@Component
public class GameHolderImpl implements GameHolder {

    private final Game game = new Game(false);

    @Override
    public Game getGame() {
        return this.game;
    }
}