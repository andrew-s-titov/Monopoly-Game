package com.monopolynew.game.procedure;

import com.monopolynew.map.PurchasableField;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class BuyProposal {

    private final UUID playerId;

    private final PurchasableField field;
}