package com.monopolynew.event;

import com.monopolynew.game.procedure.CheckToPay;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PayCommandEvent implements GameEvent {

    private final int code = 312;

    private final int sum;

    private final boolean wiseToGiveUp;

    public static PayCommandEvent of(CheckToPay checkToPay) {
        return new PayCommandEvent(checkToPay.getDebt(), checkToPay.isWiseToGiveUp());
    }
}
