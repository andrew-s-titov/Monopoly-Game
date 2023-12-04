package com.monopolynew.exception;

import lombok.Getter;

public enum ExceptionCode {
    PLAYER_INPUT(401),
    CLIENT_REQUEST(402);

    @Getter
    private final int code;

    ExceptionCode(int code) {
        this.code = code;
    }
}
