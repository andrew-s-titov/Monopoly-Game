package com.monopolynew.service;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.map.GameField;

import java.util.List;

public interface GameFieldConverter {

    GameFieldView toView(GameField gameField);

    List<GameFieldView> toListView(List<GameField> gameFieldList);

    List<GameFieldView> toListView(GameField[] gameFieldArray);
}
