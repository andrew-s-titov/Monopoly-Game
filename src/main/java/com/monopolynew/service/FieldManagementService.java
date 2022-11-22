package com.monopolynew.service;

import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;

import java.util.List;

public interface FieldManagementService {

    List<FieldManagementAction> availableManagementActions(Game game, int fieldId, String playerId);

    void mortgageField(Game game, int fieldId, String playerId);

    void redeemMortgagedProperty(Game game, int fieldId, String playerId);

    void buyHouse(Game game, int fieldId, String playerId);

    void sellHouse(Game game, int fieldId, String playerId);

    boolean housePurchaseAvailable(Game game, Player player, StreetField streetField);

    boolean houseSaleAvailable(Game game, StreetField streetField);

    boolean mortgageAvailable(Game game, PurchasableField purchasableField);

    boolean redeemAvailable(Game game, PurchasableField purchasableField);
}
