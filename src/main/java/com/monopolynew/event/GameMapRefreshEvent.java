package com.monopolynew.event;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameMapRefreshEvent implements GameEvent {

    private final int code = 300;

    private final Collection<Player> players;

    private final List<GameFieldView> fields;

    private final String currentPlayer;
}