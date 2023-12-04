package com.monopolynew.exception;

import lombok.Getter;

public class ClientBadRequestException extends BadRequestException {

    @Getter
    private final int code = ExceptionCode.CLIENT_REQUEST.getCode();

    public ClientBadRequestException(String message) {
        super(message);
    }
}
