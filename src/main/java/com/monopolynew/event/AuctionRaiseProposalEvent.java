package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionRaiseProposalEvent implements GameEvent {

    private final int code = 309;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("field_name")
    private final String fieldName;

    private final int proposal;

    public static AuctionRaiseProposalEvent fromAuction(Auction auction) {
        return new AuctionRaiseProposalEvent(
                auction.getCurrentParticipant().getId(),
                auction.getField().getName(),
                auction.getAuctionPrice() + Rules.AUCTION_STEP
        );
    }
}