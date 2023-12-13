package com.monopolynew.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AuctionBuyProposalEvent implements GameEvent {

    private final int code = 310;

    private final UUID playerId;

    private final int fieldIndex;

    private final int proposal;
}