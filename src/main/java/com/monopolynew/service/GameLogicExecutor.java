package com.monopolynew.service;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import org.springframework.lang.Nullable;

public interface GameLogicExecutor {

    void movePlayer(Game game, Player player, int newPositionIndex, boolean forward);

    void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason);

    void sendBuyProposal(Game game, Player player, PurchasableField field, boolean payable);

    void doBuyField(Game game, PurchasableField field, int price, String buyerId);

    int computePlayerAssets(Game game, Player player);

    int computeNewPlayerPosition(Player player, DiceResult diceResult);

    void endTurn(Game game);

    void bankruptPlayerToCreditor(Game game, Player player, Integer remainingAssets);

    void bankruptPlayerToState(Game game, Player player);
}
