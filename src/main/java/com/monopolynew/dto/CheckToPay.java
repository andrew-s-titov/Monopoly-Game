package com.monopolynew.dto;

import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CheckToPay {

    private final Player player;

    private final Player beneficiary;

    private final int sum;

    private final boolean payable;

    private final boolean wiseToGiveUp;

    private final String comment;
}