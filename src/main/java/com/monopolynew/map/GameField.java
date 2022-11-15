package com.monopolynew.map;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface GameField {
    @JsonProperty("id")
    int getId();
    @JsonProperty("name")
    String getName();
}