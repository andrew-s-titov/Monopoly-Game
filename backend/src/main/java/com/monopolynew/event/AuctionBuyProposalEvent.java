package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionBuyProposalEvent implements GameEvent {

    private final int code = 310;

    private final int fieldIndex;

    private final int proposal;
}