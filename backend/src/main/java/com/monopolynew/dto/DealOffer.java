package com.monopolynew.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class DealOffer {

    private final List<Integer> initiatorFields;

    private final List<Integer> addresseeFields;

    @PositiveOrZero
    private final Integer initiatorMoney;

    @PositiveOrZero
    private final Integer addresseeMoney;
}
