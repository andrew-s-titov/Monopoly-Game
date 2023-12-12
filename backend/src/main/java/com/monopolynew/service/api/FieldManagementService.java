package com.monopolynew.service.api;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;

import java.util.UUID;

public interface FieldManagementService {

    void mortgageField(Game game, int fieldIndex, UUID playerId);

    void redeemMortgagedProperty(Game game, int fieldIndex, UUID playerId);

    void buyHouse(Game game, int fieldIndex, UUID playerId);

    void sellHouse(Game game, int fieldIndex, UUID playerId);

    boolean housePurchaseAvailable(Game game, Player player, StreetField streetField);

    boolean houseSaleAvailable(Game game, StreetField streetField);

    boolean mortgageAvailable(Game game, PurchasableField purchasableField);

    boolean redemptionAvailable(Game game, PurchasableField purchasableField);
}
