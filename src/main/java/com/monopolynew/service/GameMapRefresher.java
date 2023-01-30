package com.monopolynew.service;

import com.monopolynew.game.Game;

public interface GameMapRefresher {

    void restoreGameStateForPlayer(Game game, String playerId);
}