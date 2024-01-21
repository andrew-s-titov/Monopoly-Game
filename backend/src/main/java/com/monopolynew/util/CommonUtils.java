package com.monopolynew.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class CommonUtils {

    private static final String NULL_ARG_MESSAGE = "Null argument passed";

    public static void requireNotNullArgs(Object... args) {
        for (Object arg : args) {
            Objects.requireNonNull(arg, NULL_ARG_MESSAGE);
        }
    }
}
