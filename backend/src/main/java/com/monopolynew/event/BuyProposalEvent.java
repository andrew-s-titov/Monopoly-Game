package com.monopolynew.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class BuyProposalEvent implements GameEvent {

    private final int code = 306;

    private final String playerId;

    private final int fieldIndex;

    private final int price;

    private final boolean payable;
}