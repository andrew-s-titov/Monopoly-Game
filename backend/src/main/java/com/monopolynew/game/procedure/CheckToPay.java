package com.monopolynew.game.procedure;

import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CheckToPay {

    private final Player debtor;

    private final Player beneficiary;

    private final int debt;

    private final boolean wiseToGiveUp;

    private final SystemMessageEvent comment;
}