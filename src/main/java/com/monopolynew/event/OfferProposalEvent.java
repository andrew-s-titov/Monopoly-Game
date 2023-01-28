package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.dto.GameFieldOfferView;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OfferProposalEvent implements GameEvent {

    private final int code = 316;

    private final String initiatorName;

    private final List<GameFieldOfferView> fieldsToBuy;

    private final List<GameFieldOfferView> fieldsToSell;

    private final Integer moneyToGive;

    private final Integer moneyToReceive;
}