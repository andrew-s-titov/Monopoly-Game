package com.monopolynew.mapper;

import com.monopolynew.dto.GameFieldState;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GameFieldMapper {

    @Mapping(target = "priceTag", source = ".", qualifiedByName = "definePriceTag")
    @Mapping(target = "ownerId", source = "owner", qualifiedByName = "getOwnerId")
    @Mapping(target = "houses", source = ".", qualifiedByName = "getHouses")
    GameFieldState toState(PurchasableField purchasableField);

    List<GameFieldState> toStateList(Collection<? extends PurchasableField> gameFieldList);

    @Named("definePriceTag")
    default String definePriceTag(PurchasableField field) {
        if (field.isFree()) {
            return "$ " + field.getPrice();
        } else {
            if (field.isMortgaged()) {
                return Integer.toString(field.getMortgageTurnsLeft());
            } else if (field instanceof StaticRentField staticRentField) {
                return "$ " + staticRentField.getCurrentRent();
            } else if (field instanceof UtilityField utilityField) {
                return "x" + utilityField.getCurrentMultiplier();
            } else {
                return null;
            }
        }
    }

    @Named("getOwnerId")
    default String getOwnerId(Player player) {
        return player == null ? null : player.getId();
    }

    @Named("getHouses")
    default Integer getHouses(PurchasableField field) {
        return field instanceof StreetField streetField
                ? streetField.getHouses()
                : null;
    }
}
