package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.PurchasableField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BuyProposalEvent implements WebsocketEvent {

    private final int code = 306;

    private final String playerId;

    private final String fieldName;

    private final int price;

    public static BuyProposalEvent fromProposal(BuyProposal buyProposal) {
        PurchasableField field = buyProposal.getField();
        return new BuyProposalEvent(buyProposal.getPlayer().getId(), field.getName(), field.getPrice());
    }
}