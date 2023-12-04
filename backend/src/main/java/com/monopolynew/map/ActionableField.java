package com.monopolynew.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActionableField implements GameField {
    private final int id;
    private final FieldAction action;

    public String getName() {
        return action.getName();
    }
}