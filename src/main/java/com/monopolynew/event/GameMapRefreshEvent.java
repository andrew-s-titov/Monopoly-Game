package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class GameMapRefreshEvent implements WebsocketEvent {

    private final int code = 300;

    private final Collection<Player> players;

    private final Object[] fields;

    @JsonProperty("current_player")
    private final String currentPlayer;
}
