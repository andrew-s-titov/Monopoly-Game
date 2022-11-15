package com.monopolynew.service;

import com.monopolynew.event.WebsocketEvent;
import com.monopolynew.game.Game;

import java.util.List;
import java.util.function.Function;

public interface ChanceContainer {

    List<Function<Game, List<WebsocketEvent>>> getChances();
}
