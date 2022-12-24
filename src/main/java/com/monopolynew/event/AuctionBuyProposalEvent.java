package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.state.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionBuyProposalEvent implements WebsocketEvent {

    private final int code = 310;

    @JsonProperty("field_name")
    private final String fieldName;

    private final int proposal;

    public static AuctionBuyProposalEvent fromAuction(Auction auction) {
        return new AuctionBuyProposalEvent(
                auction.getField().getName(),
                auction.getAuctionPrice()
        );
    }
}