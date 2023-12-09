package com.monopolynew.exception;

import com.monopolynew.enums.BadRequestCode;
import lombok.Getter;

public class PlayerInvalidInputException extends BadRequestException {

    @Getter
    private final int code = BadRequestCode.PLAYER_INPUT.getCode();

    public PlayerInvalidInputException(String message) {
        super(message);
    }
}