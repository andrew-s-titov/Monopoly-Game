package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.dto.CheckToPay;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PayCommandEvent implements WebsocketEvent {

    private final int code = 312;

    private final String playerId;

    private final int sum;

    private final boolean payable;

    private final boolean wiseToGiveUp;

    public static PayCommandEvent fromCheck(CheckToPay checkToPay) {
        var player = checkToPay.getPlayer();
        var sum = checkToPay.getSum();
        return new PayCommandEvent(player.getId(), sum, player.getMoney() >= sum, checkToPay.isWiseToGiveUp());
    }
}