package com.monopolynew.exception;

import com.monopolynew.enums.BadRequestCode;
import lombok.Getter;

public class UserInvalidInputException extends BadRequestException {

    @Getter
    private final int code = BadRequestCode.PLAYER_INPUT.getCode();

    public UserInvalidInputException(String message) {
        super(message);
    }
}