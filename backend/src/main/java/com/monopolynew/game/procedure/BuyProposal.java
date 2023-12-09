package com.monopolynew.game.procedure;

import com.monopolynew.map.PurchasableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class BuyProposal {

    private final String playerId;

    private final PurchasableField field;

    @Setter
    private boolean payable;
}