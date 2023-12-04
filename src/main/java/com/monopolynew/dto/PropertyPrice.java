package com.monopolynew.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class PropertyPrice {

    private int housesPrice;

    private int fieldsPrice;

    private int total;

    @Builder
    public PropertyPrice(int housesPrice, int fieldsPrice) {
        this.housesPrice = housesPrice;
        this.fieldsPrice = fieldsPrice;
        this.total = housesPrice + fieldsPrice;
    }
}
