package com.monopolynew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerMessageDTO {

    private final String playerId;

    private final String message;
}