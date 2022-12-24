package com.monopolynew.service;

import com.monopolynew.dto.GameFieldOfferView;
import com.monopolynew.dto.GameFieldView;
import com.monopolynew.map.GameField;

import java.util.List;

public interface GameFieldConverter {

    <T extends GameField> GameFieldView toView(T gameField);

    <T extends GameField> List<GameFieldView> toListView(List<T> gameFieldList);

    <T extends GameField> List<GameFieldOfferView> toListOfferView(List<T> gameFieldList);
}
