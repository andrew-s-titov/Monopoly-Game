package com.monopolynew.util;

@FunctionalInterface
public interface TriConsumer<F, S, T> {

    void apply(F first, S second, T third);
}