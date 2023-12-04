package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.map.GameField;

public interface StepProcessor {

    void processStepOnField(Game game, GameField field);
}
