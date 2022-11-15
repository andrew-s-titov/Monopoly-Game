package com.monopolynew.map;

import com.monopolynew.game.Player;

public interface PurchasableField extends GameField {
    int getPrice();
    int getGroup();
    Player getOwner();
    boolean isFree();
    void newOwner(Player player);
    boolean isMortgaged();
    void pledge();
    int decreaseMortgageTurns();
}