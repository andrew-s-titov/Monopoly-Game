package com.monopolynew.service;

import com.monopolynew.event.WebsocketEvent;
import com.monopolynew.game.Game;

import java.util.List;

public interface ChanceManager {

    List<WebsocketEvent> applyRandomChance(Game game);
}
