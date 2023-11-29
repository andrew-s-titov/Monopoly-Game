package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
public class GameFieldView {

    private final int id;

    private final String name;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Integer group;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Integer houses;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String ownerId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String priceTag;

    private final boolean mortgage;
}
