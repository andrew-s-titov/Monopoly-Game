package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;

public interface StepProcessor {

    void processStepOnPurchasableField(Game game, Player player, PurchasableField field);
}
