package com.monopolynew.util;

import java.util.Objects;

public class Utils {

    private static final String NULL_ARG_MESSAGE = "Null argument passed";

    public static void requireNotNullArgs(Object... args) {
        for (Object arg : args) {
            Objects.requireNonNull(arg, NULL_ARG_MESSAGE);
        }
    }

    private Utils() {
        // NO-OP
    }
}
