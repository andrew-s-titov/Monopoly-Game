package com.monopolynew.enums;

import lombok.Getter;

public enum BadRequestCode {
    PLAYER_INPUT(401),
    CLIENT_REQUEST(402);

    @Getter
    private final int code;

    BadRequestCode(int code) {
        this.code = code;
    }
}
