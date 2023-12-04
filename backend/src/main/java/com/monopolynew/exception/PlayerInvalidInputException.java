package com.monopolynew.exception;

import lombok.Getter;

public class PlayerInvalidInputException extends BadRequestException {

    @Getter
    private final int code = ExceptionCode.PLAYER_INPUT.getCode();

    public PlayerInvalidInputException(String message) {
        super(message);
    }
}