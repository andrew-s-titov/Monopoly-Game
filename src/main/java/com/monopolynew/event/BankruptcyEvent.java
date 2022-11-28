package com.monopolynew.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BankruptcyEvent implements WebsocketEvent {

    private final int code = 311;

    private final String playerId;

    public static BankruptcyEvent forPlayer(Player player) {
        return new BankruptcyEvent(player.getId());
    }
}
