package com.monopolynew.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JailAction {
    PAY,
    LUCK;

    @JsonCreator
    public static JailAction fromString(String actionName) {
        for (JailAction action : JailAction.values()) {
            if (action.name().equalsIgnoreCase(actionName)) {
                return action;
            }
        }
        throw new IllegalArgumentException(
                String.format("No enum constant of %s matches '%s'", JailAction.class.getName(), actionName));
    }
}