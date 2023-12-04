package com.monopolynew.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MortgageChange {

    private final int fieldIndex;

    private final int turns;
}