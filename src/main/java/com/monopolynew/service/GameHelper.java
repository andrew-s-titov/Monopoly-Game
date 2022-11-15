package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import org.springframework.lang.Nullable;

public interface GameHelper {

    int movePlayer(Game game, Player player);

    void sendToJail(Game game, Player player, @Nullable String reason);

    void doBuyField(PurchasableField field, int price, Player player);

    int computePlayerAssets(Game game, Player player);

    void endTurn(Game game);
}
