package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionRaiseProposalEvent implements GameEvent {

    private final int code = 309;

    private final int fieldIndex;

    private final int proposal;
}