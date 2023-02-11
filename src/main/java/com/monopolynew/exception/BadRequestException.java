package com.monopolynew.exception;

public abstract class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public abstract int getCode();
}
