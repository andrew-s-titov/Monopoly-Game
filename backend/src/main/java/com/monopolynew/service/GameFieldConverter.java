package com.monopolynew.service;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.map.PurchasableField;

import java.util.List;

public interface GameFieldConverter {

    GameFieldState toView(PurchasableField gameField);

    List<GameFieldState> toListView(List<PurchasableField> gameFieldList);
}
