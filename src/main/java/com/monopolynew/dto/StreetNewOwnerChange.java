package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StreetNewOwnerChange {

    @JsonProperty("player_id")
    private final String playerId;

    private final int field;
}