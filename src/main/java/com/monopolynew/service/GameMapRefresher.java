package com.monopolynew.service;

import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.game.Game;

public interface GameMapRefresher {

    GameMapRefreshEvent getRefreshEvent(Game game);
}