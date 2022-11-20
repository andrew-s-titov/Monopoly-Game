package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("owner_id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String ownerId;

    @JsonProperty("price_tag")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String priceTag;
}
