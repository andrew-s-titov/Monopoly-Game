package com.monopolynew.map;

import com.monopolynew.game.Player;

public interface PurchasableField extends GameField {

    int getPrice();

    int getGroupId();

    Player getOwner();

    boolean isFree();

    void newOwner(Player player);

    boolean isMortgaged();

    /**
     * @return true if this field is mortgaged during current turn, false if not. If true is returned, next invocation
     * will return false (the flag is dropped)
     */
    boolean isMortgagedDuringThisTurn();

    void mortgage();

    void redeem();

    int decreaseMortgageTurns();

    int getMortgageTurnsLeft();
}