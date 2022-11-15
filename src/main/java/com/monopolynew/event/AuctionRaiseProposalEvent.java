package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionRaiseProposalEvent implements WebsocketEvent {

    private final int code = 309;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("field_name")
    private final String fieldName;

    private final int proposal;

    public static AuctionRaiseProposalEvent propose(Player player, String fieldName, int currentAuctionPrice) {
        return new AuctionRaiseProposalEvent(player.getId(), fieldName, currentAuctionPrice + Rules.AUCTION_STEP);
    }
}