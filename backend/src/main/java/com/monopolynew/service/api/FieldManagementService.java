package com.monopolynew.service.api;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;

public interface FieldManagementService {

    void mortgageField(Game game, int fieldIndex, String playerId);

    void redeemMortgagedProperty(Game game, int fieldIndex, String playerId);

    void buyHouse(Game game, int fieldIndex, String playerId);

    void sellHouse(Game game, int fieldIndex, String playerId);

    boolean housePurchaseAvailable(Game game, Player player, StreetField streetField);

    boolean houseSaleAvailable(Game game, StreetField streetField);

    boolean mortgageAvailable(Game game, PurchasableField purchasableField);

    boolean redemptionAvailable(Game game, PurchasableField purchasableField);
}
