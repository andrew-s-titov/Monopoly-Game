package com.monopolynew.dto;

import java.util.UUID;

public record GameResponseDTO(UUID gameId) {

    public static GameResponseDTO withId(UUID gameId) {
        return new GameResponseDTO(gameId);
    }
}
