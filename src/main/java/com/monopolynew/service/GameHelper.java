package com.monopolynew.service;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import org.springframework.lang.Nullable;

public interface GameHelper {

    void movePlayer(Game game, Player player, int newPositionIndex, boolean forward);

    void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason);

    void sendBuyProposal(Game game, Player player, PurchasableField field);

    void doBuyField(Game game, PurchasableField field, int price, Player player);

    int computePlayerAssets(Game game, Player player);

    int computeNewPlayerPosition(Player player, DiceResult diceResult);

    void endTurn(Game game);

    void bankruptPlayer(Game game, Player player);
}
