package com.monopolynew.service.api;

import com.monopolynew.game.Game;

import java.util.UUID;

public interface GameMapRefresher {

    void restoreGameStateForPlayer(Game game, UUID playerId);
}