package com.monopolynew.service.impl;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameFieldConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameFieldConverterImpl implements GameFieldConverter {

    @Override
    public GameFieldState toView(PurchasableField purchasableField) {
        int id = purchasableField.getId();
        String priceTag = null;
        String ownerId = null;
        Integer houses = null;
        boolean mortgage = false;

        Integer group = PurchasableFieldGroups.getGroupIdByFieldIndex(purchasableField.getId());
        if (purchasableField.isFree()) {
            priceTag = "$ " + purchasableField.getPrice();
        } else {
            ownerId = purchasableField.getOwner().getId();
            if (purchasableField.isMortgaged()) {
                mortgage = true;
                priceTag = Integer.toString(purchasableField.getMortgageTurnsLeft());
            } else if (purchasableField instanceof StaticRentField staticRentField) {
                priceTag = "$ " + staticRentField.getCurrentRent();
            } else if (purchasableField instanceof UtilityField utilityField) {
                priceTag = "x" + utilityField.getCurrentMultiplier();
            }
        }
        if (purchasableField instanceof StreetField streetField) {
            houses = streetField.getHouses();
        }

        return GameFieldState.builder()
                .id(id)
                .name(purchasableField.getName())
                .group(group)
                .ownerId(ownerId)
                .mortgage(mortgage)
                .priceTag(priceTag)
                .houses(houses)
                .build();
    }

    @Override
    public List<GameFieldState> toListView(List<PurchasableField> gameFieldList) {
        return gameFieldList.stream()
                .map(this::toView)
                .toList();
    }
}