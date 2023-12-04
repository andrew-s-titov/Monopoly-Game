package com.monopolynew.dto;

import com.monopolynew.game.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
public class CheckToPay {

    private final Player debtor;

    private final Player beneficiary;

    private final int debt;

    @Setter
    private boolean payable;

    private final boolean wiseToGiveUp;

    private final String comment;
}