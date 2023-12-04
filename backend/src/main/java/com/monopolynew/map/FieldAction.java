package com.monopolynew.map;

import lombok.Getter;

public enum FieldAction {
    START("Start"),
    CHANCE("Chance"),
    JAIL("Jail"),
    ARRESTED("Arrested"),
    TELEPORT("Teleport"),
    PARKING("Parking"),
    LUXURY_TAX("Luxury Tax"),
    INCOME_TAX("Income Tax");

    @Getter
    private final String name;

    FieldAction(String name) {
        this.name = name;
    }
}