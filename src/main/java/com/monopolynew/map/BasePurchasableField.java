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
    private final int group;
    @Getter
    private final int price;

    @Getter
    private Player owner;
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

    public void pledge() {
        this.mortgageTurns = Rules.MORTGAGE_TURNS;
    }

    public final int decreaseMortgageTurns() {
        if (isMortgaged()) {
            mortgageTurns--;
            if (mortgageTurns == 0) {
                owner = null;
            }
        }
        return mortgageTurns;
    }
}