package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Getter
@Builder
public class SystemMessageEvent {

    private final int code = 201;
    private final String translationKey;
    private final Map<String, Object> params;

    @JsonCreator
    public SystemMessageEvent(@NonNull String translationKey, @Nullable Map<String, Object> params) {
        this.translationKey = translationKey;
        this.params = params;
    }

    public SystemMessageEvent(@NonNull String translationKey) {
        this(translationKey, null);
    }
}