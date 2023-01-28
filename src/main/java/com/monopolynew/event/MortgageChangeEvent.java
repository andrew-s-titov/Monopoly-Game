package com.monopolynew.event;

import com.monopolynew.dto.MortgageChange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MortgageChangeEvent implements GameEvent {

    private final int code = 313;

    private final List<MortgageChange> changes;
}
