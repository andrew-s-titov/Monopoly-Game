package com.monopolynew.map;

import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BasePurchasableField implements PurchasableField {

    @Getter
    protected final int id;
    @Getter
    protected final String name;
    @Getter
    private final int groupId;
    @Getter
    private final int price;

    @Getter
    private Player owner;
    private boolean mortgagedDuringThisTurn = false;
    protected int mortgageTurns = 0;

    public final boolean isFree() {
        return owner == null;
    }

    public final void newOwner(Player player) {
        this.owner = player;
    }

    public final boolean isMortgaged() {
        return this.mortgageTurns > 0;
    }

    public final boolean isMortgagedDuringThisTurn() {
        boolean result = mortgagedDuringThisTurn;
        mortgagedDuringThisTurn = false;
        return result;
    }

    public void mortgage() {
        this.mortgageTurns = Rules.MORTGAGE_TURNS;
        this.mortgagedDuringThisTurn = true;
    }

    public final void redeem() {
        if (!isMortgaged()) {
            throw new IllegalStateException("cannot redeem not mortgaged field");
        }
        mortgagedDuringThisTurn = false;
        this.mortgageTurns = 0;
    }

    public final int decreaseMortgageTurns() {
        mortgagedDuringThisTurn = false;
        if (isMortgaged()) {
            mortgageTurns--;
            if (mortgageTurns == 0) {
                owner = null;
            }
        }
        return mortgageTurns;
    }

    public final int getMortgageTurnsLeft() {
        return this.mortgageTurns;
    }
}