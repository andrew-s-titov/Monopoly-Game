package com.monopolynew.service.impl;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameFieldConverter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameFieldConverterImpl implements GameFieldConverter {

    public GameFieldView toView(GameField gameField) {
        int id = gameField.getId();
        String priceTag = null;
        String ownerId = null;
        Integer group = null;
        if (gameField instanceof PurchasableField) {
            PurchasableField purchasableField = (PurchasableField) gameField;
            group = purchasableField.getGroup();
            if ((purchasableField).isFree()) {
                priceTag = "$ " + purchasableField.getPrice();
            } else {
                ownerId = purchasableField.getOwner().getId();
                if (gameField instanceof StaticRentField) {
                    priceTag = "$ " + ((StaticRentField) gameField).getCurrentRent();
                } else if (gameField instanceof UtilityField) {
                    priceTag = "x" + ((UtilityField) gameField).getCurrentMultiplier();
                }
            }
        }
        return GameFieldView.builder()
                .id(id)
                .name(gameField.getName())
                .group(group)
                .ownerId(ownerId)
                .priceTag(priceTag)
                .build();
    }

    public List<GameFieldView> toListView(List<GameField> gameFieldList) {
        return gameFieldList.stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    public List<GameFieldView> toListView(GameField[] gameFieldArray) {
        return Arrays.stream(gameFieldArray)
                .map(this::toView)
                .collect(Collectors.toList());
    }
}