package com.monopolynew.service;

import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class GameRepository {

    private Game game = new Game(false);

    public Game getGame() {
        // TODO get game by its ID
        return this.game;
    }

    public UUID createGame(NewGameParamsDTO newGameParamsDTO) {
        // TODO: implement map for multi-game setup
        return game.getId();
    }

    public void removeGame(UUID gameId) {
        // TODO: implement for multi-game env
    }

    public void endGame() {
        // temporary decision before multiple game rooms
        Collection<Player> players = this.game.getPlayers();
        this.game = new Game(false);
        players.forEach(player -> {
            player.resetState();
            game.addPlayer(player);
        });
    }
}