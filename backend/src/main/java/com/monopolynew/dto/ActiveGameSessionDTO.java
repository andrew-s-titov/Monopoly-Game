package com.monopolynew.dto;

import java.util.UUID;

public record ActiveGameSessionDTO(UUID gameId) {

    public static ActiveGameSessionDTO withId(UUID gameId) {
        return new ActiveGameSessionDTO(gameId);
    }
}
