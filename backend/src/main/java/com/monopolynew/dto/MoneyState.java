package com.monopolynew.dto;

import com.monopolynew.game.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class MoneyState {

    private final UUID playerId;

    private final int money;

    public static MoneyState fromPlayer(Player player) {
        return new MoneyState(player.getId(), player.getMoney());
    }
}