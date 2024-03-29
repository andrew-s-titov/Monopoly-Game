package com.monopolynew.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class OfferProposalEvent implements GameEvent {

    private final int code = 316;

    private final String initiatorName;

    private final List<Integer> addresseeFields;

    private final List<Integer> initiatorFields;

    private final Integer addresseeMoney;

    private final Integer initiatorMoney;
}
