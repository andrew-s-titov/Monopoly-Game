package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MoneyState {

    @JsonProperty("player_id")
    private final String playerId;

    private final int money;

    public static MoneyState fromPlayer(Player player) {
        return new MoneyState(player.getId(), player.getMoney());
    }
}