package com.monopolynew.game;

public class Rules {
    public static final int MAX_PLAYERS = 5;
    public static final int START_MONEY_AMOUNT = 1500;
    public static final int CIRCLE_MONEY = 200;
    public static final int MORTGAGE_TURNS = 15;
    public static final int MAX_HOUSES_ON_STREET = 5;
    public static final int INCOME_TAX = CIRCLE_MONEY;
    public static final int LUXURY_TAX = 100;
    public static final int DOUBLETS_LIMIT = 3;
    public static final int TURNS_IN_JAIL = 3;
    public static final int JAIL_BAIL = 50;
    public static final int AUCTION_STEP = 10;

    public static final int NUMBER_OF_FIELDS = 40;
    public static final int LAST_FIELD_INDEX = NUMBER_OF_FIELDS - 1;
    public static final int JAIL_FIELD_NUMBER = 10;

    public static final int STANDARD_UTILITY_MULTIPLIER = 4;
    public static final int INCREASED_UTILITY_MULTIPLIER = 10;

    private Rules() {
        // NO-OP
    }
}