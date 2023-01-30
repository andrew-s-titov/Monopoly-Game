package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionBuyProposalEvent implements GameEvent {

    private final int code = 310;

    @JsonProperty("field_name")
    private final String fieldName;

    private final int proposal;
}