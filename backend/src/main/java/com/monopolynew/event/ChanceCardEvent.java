package com.monopolynew.event;

import lombok.Getter;

import java.util.Map;

@Getter
public class ChanceCardEvent implements GameEvent {

    private final int code = 202;

    private final String translationKey;
    private final Map<String, Object> params;

    public ChanceCardEvent(String translationKey, Map<String, Object> params) {
        this.translationKey = translationKey;
        this.params = params;
    }

    public ChanceCardEvent(String translationKey) {
        this(translationKey, null);
    }
}
