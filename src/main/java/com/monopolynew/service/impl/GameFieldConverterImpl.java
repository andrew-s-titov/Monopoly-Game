package com.monopolynew.service.impl;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameFieldConverter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameFieldConverterImpl implements GameFieldConverter {

    public <T extends GameField> GameFieldView toView(T gameField) {
        int id = gameField.getId();
        String priceTag = null;
        String ownerId = null;
        Integer group = null;
        Integer houses = null;
        boolean mortgage = false;
        if (gameField instanceof PurchasableField) {
            PurchasableField purchasableField = (PurchasableField) gameField;
            group = purchasableField.getGroupId();
            if (purchasableField.isFree()) {
                priceTag = "$ " + purchasableField.getPrice();
            } else {
                ownerId = purchasableField.getOwner().getId();
                if (purchasableField.isMortgaged()) {
                    mortgage = true;
                    priceTag = Integer.toString(purchasableField.getMortgageTurnsLeft());
                } else if (gameField instanceof StaticRentField) {
                    priceTag = "$ " + ((StaticRentField) gameField).getCurrentRent();
                } else if (gameField instanceof UtilityField) {
                    priceTag = "x" + ((UtilityField) gameField).getCurrentMultiplier();
                }
            }
            if (purchasableField instanceof StreetField) {
                houses = ((StreetField) purchasableField).getHouses();
            }
        }
        return GameFieldView.builder()
                .id(id)
                .name(gameField.getName())
                .group(group)
                .ownerId(ownerId)
                .mortgage(mortgage)
                .priceTag(priceTag)
                .houses(houses)
                .build();
    }

    public <T extends GameField> List<GameFieldView> toListView(List<T> gameFieldList) {
        return gameFieldList.stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    public <T extends GameField> List<GameFieldView> toListView(T[] gameFieldArray) {
        return Arrays.stream(gameFieldArray)
                .map(this::toView)
                .collect(Collectors.toList());
    }
}