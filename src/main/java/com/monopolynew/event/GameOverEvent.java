package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameOverEvent implements GameEvent {

    private final int code = 315;

    private final String playerId;

    private final String playerName;

    public static GameOverEvent withWinner(Player player) {
        return new GameOverEvent(player.getId(), player.getName());
    }
}
