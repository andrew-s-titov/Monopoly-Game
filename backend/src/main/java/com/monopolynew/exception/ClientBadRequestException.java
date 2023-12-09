package com.monopolynew.exception;

import com.monopolynew.enums.BadRequestCode;
import lombok.Getter;

public class ClientBadRequestException extends BadRequestException {

    @Getter
    private final int code = BadRequestCode.CLIENT_REQUEST.getCode();

    public ClientBadRequestException(String message) {
        super(message);
    }
}
