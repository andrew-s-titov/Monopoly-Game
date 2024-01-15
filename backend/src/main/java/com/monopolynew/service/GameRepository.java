package com.monopolynew.service;

import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.game.Game;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GameRepository {

    private final Map<UUID, Game> games = new HashMap<>();

    public Game getGame() {
        // TODO get game by its ID
        return getOrCreateGame();
    }

    public UUID createGame(NewGameParamsDTO newGameParamsDTO) {
        // TODO: implement map for multi-game setup
        return getOrCreateGame().getId();
    }

    public void removeGame() {
        games.clear();
        // TODO: implement for multi-game env
    }

    public Collection<Game> allGames() {
        return games.values();
    }

    // temporary thing for single-room
    private Game getOrCreateGame() {
        if (games.values().isEmpty()) {
            var newGame = new Game(false);
            games.put(newGame.getId(), newGame);
        }
        return games.values().iterator().next();
    }
}