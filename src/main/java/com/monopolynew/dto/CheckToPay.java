package com.monopolynew.dto;

import com.monopolynew.game.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class CheckToPay {

    private final Player player;

    private final Player beneficiary;

    private final int sum;

    @Setter
    private boolean payable;

    private final boolean wiseToGiveUp;

    private final String comment;
}