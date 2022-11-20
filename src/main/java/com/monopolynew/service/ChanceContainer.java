package com.monopolynew.service;

import com.monopolynew.game.Game;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.function.Consumer;

public interface ChanceContainer {

    @NonNull
    List<Consumer<Game>> getChances();
}
