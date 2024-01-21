package com.monopolynew.service;

import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.game.Game;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRepository {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public Game findGame(@NonNull UUID gameId) {
        return games.get(gameId);
    }

    public UUID createGame(NewGameParamsDTO newGameParamsDTO) {
        var newGame = new Game(newGameParamsDTO.isWithTeleport());
        UUID gameId = newGame.getId();
        games.put(gameId, newGame);
        return gameId;
    }

    public void removeGame(@NonNull UUID gameId) {
        games.remove(gameId);
    }

    public Collection<Game> allGames() {
        return games.values();
    }
}