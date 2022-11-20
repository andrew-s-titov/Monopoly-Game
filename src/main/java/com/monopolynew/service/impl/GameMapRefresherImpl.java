package com.monopolynew.service.impl;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.game.Game;
import com.monopolynew.service.GameFieldConverter;
import com.monopolynew.service.GameMapRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GameMapRefresherImpl implements GameMapRefresher {

    private final GameFieldConverter gameFieldConverter;

    @Override
    public GameMapRefreshEvent getRefreshEvent(Game game) {
        List<GameFieldView> fieldViews = gameFieldConverter.toListView(game.getGameMap().getFields());
        String currentPlayerId = game.getCurrentPlayer().getId();
        return new GameMapRefreshEvent(game.getPlayers(), fieldViews, currentPlayerId);
    }
}
