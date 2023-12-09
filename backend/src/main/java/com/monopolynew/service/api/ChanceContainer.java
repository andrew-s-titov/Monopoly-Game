package com.monopolynew.service.api;

import org.springframework.lang.NonNull;

import java.util.List;

public interface ChanceContainer {

    @NonNull
    List<ChanceCard> getChances();
}
