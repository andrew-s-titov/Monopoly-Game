package com.monopolynew.dto;

import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MoneyState {

    private final String playerId;

    private final int money;

    public static MoneyState fromPlayer(Player player) {
        return new MoneyState(player.getId(), player.getMoney());
    }
}