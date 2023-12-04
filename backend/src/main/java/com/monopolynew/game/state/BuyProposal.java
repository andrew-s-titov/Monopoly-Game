package com.monopolynew.game.state;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.map.PurchasableField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BuyProposal {

    private final String playerId;

    private final PurchasableField field;

    @Setter
    private boolean payable;
}