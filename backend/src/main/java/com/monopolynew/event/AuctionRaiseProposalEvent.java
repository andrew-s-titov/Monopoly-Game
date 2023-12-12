package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AuctionRaiseProposalEvent implements GameEvent {

    private final int code = 309;

    private final UUID playerId;

    private final int fieldIndex;

    private final int proposal;
}