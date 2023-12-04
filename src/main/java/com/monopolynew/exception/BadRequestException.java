package com.monopolynew.exception;

public abstract class BadRequestException extends RuntimeException {

    BadRequestException(String message) {
        super(message);
    }

    public abstract int getCode();
}
