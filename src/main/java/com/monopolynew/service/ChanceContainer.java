package com.monopolynew.service;

import com.monopolynew.game.Game;

import java.util.List;
import java.util.function.Consumer;

public interface ChanceContainer {

    List<Consumer<Game>> getChances();
}
