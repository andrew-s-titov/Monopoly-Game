package com.monopolynew.service.api;

import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.enums.GameStage;
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

    void bankruptPlayer(Game game, Player player);

    void changeGameStage(Game game, GameStage newGameStage);

    int getFieldMortgagePrice(PurchasableField field);
}
