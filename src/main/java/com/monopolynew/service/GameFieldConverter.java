package com.monopolynew.service;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.map.PurchasableField;

import java.util.List;

public interface GameFieldConverter {

    GameFieldView toView(PurchasableField gameField);

    List<GameFieldView> toListView(List<PurchasableField> gameFieldList);
}
