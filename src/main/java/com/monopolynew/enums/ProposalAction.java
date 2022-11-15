package com.monopolynew.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProposalAction {
    ACCEPT,
    DECLINE;

    @JsonCreator
    public static ProposalAction fromString(String actionName) {
        for (ProposalAction action : ProposalAction.values()) {
            if (action.name().equalsIgnoreCase(actionName)) {
                return action;
            }
        }
        throw new IllegalArgumentException(
                String.format("No enum constant of %s matches '%s'", ProposalAction.class.getName(), actionName));
    }
}