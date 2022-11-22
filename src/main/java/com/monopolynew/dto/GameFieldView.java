package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
