package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import org.springframework.lang.Nullable;

public interface GameHelper {

    void movePlayerForward(Game game, Player player, int newPosition);

    void changePlayerPosition(Player player, int fieldId);

    void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason);

    void sendBuyProposal(Game game, Player player, PurchasableField field);

    void doBuyField(Game game, PurchasableField field, int price, Player player);

    int computePlayerAssets(Game game, Player player);

    void endTurn(Game game);
}
