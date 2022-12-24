package com.monopolynew.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PreDealInfo {

    private final List<GameFieldOfferView> offerInitiatorFields;
    private final List<GameFieldOfferView> offerAddresseeFields;
}