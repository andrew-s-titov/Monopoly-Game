package com.monopolynew.service.api;

import com.monopolynew.game.Game;

public interface GameMapRefresher {

    void restoreGameStateForPlayer(Game game, String playerId);
}