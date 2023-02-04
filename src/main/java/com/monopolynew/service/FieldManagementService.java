package com.monopolynew.service;

import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface FieldManagementService {

    List<FieldManagementAction> availableManagementActions(Game game, int fieldIndex, String playerId);

    Pair<Integer, Integer> mortgageField(Game game, int fieldIndex, String playerId);

    Pair<Integer, Integer> redeemMortgagedProperty(Game game, int fieldIndex, String playerId);

    Pair<Integer, Integer> buyHouse(Game game, int fieldIndex, String playerId);

    Pair<Integer, Integer> sellHouse(Game game, int fieldIndex, String playerId);

    boolean housePurchaseAvailable(Game game, Player player, StreetField streetField);

    boolean houseSaleAvailable(Game game, StreetField streetField);

    boolean mortgageAvailable(Game game, PurchasableField purchasableField);

    boolean redemptionAvailable(Game game, PurchasableField purchasableField);
}
