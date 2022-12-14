package com.monopolynew.service;

import com.monopolynew.game.Game;

import java.util.UUID;

public interface GameRepository {

    Game getGame();

    UUID createGame(int maxPlayers, boolean withTeleport);

    void removeGame(UUID gameId);
}