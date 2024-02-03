package com.monopolynew.event;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Getter
public class SystemMessageEvent {

    private final int code = 201;
    private final String translationKey;
    private final Map<String, Object> params;

    public SystemMessageEvent(@NonNull String translationKey, @Nullable Map<String, Object> params) {
        this.translationKey = translationKey;
        this.params = params;
    }

    public SystemMessageEvent(@NonNull String translationKey) {
        this(translationKey, null);
    }
}