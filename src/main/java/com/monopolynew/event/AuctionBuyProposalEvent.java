package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionBuyProposalEvent implements WebsocketEvent {

    private final int code = 310;

    @JsonProperty("player_id")
    private final String playerId;

    @JsonProperty("field_name")
    private final String fieldName;

    private final int proposal;
}