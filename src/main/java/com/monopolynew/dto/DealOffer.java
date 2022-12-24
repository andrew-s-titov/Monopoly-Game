package com.monopolynew.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RequiredArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DealOffer {

    private final List<Integer> fieldsToSell;

    private final List<Integer> fieldsToBuy;

    @PositiveOrZero
    private final Integer moneyToGive;

    @PositiveOrZero
    private final Integer moneyToReceive;
}
