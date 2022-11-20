package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StaticRentField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.StepProcessor;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class StepProcessorImpl implements StepProcessor {

    private final GameEventSender gameEventSender;
    private final GameHelper gameHelper;
    private final AuctionManager auctionManager;

    @Override
    public void processStepOnPurchasableField(Game game, Player player, PurchasableField field) {
        boolean rentPaymentNeeded = isRentPaymentNeeded(game, player, field);
        if (rentPaymentNeeded) {
            int rent;
            if (field instanceof StaticRentField) {
                rent = ((StaticRentField) field).getCurrentRent();
            } else if (field instanceof UtilityField) {
                rent = ((UtilityField) field).getRent(game.getLastDice());
            } else {
                throw new IllegalStateException("Failed to compute rent - unknown field type");
            }
            processRentPayment(game, player, field, rent);
        }
    }

    private boolean isRentPaymentNeeded(Game game, Player player, PurchasableField field) {
        if (field.isFree()) {
            int streetPrice = field.getPrice();
            if (streetPrice <= player.getMoney()) {
                gameHelper.sendBuyProposal(game, player, field);
            } else {
                // TODO: auto auction or let sell or pledge something?
                auctionManager.startNewAuction(game, field);
            }
            return false;
        } else if (field.isMortgaged() || player.equals(field.getOwner())) {
            gameHelper.endTurn(game);
            return false;
        } else {
            return true;
        }
    }

    private void processRentPayment(Game game, Player player, PurchasableField field, int rent) {
        int playerMoney = player.getMoney();
        Player owner = field.getOwner();
        if (playerMoney >= rent) {
            player.takeMoney(rent);
            owner.addMoney(rent);
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                    String.format("%s is paying %s $%s rent for %s",
                            player.getName(), owner.getName(), rent, field.getName())));
            gameEventSender.sendToAllPlayers(
                    new MoneyChangeEvent(
                            List.of(MoneyState.fromPlayer(player), MoneyState.fromPlayer(owner)))
            );
            gameHelper.endTurn(game);
        } else {
            int playersAssets = gameHelper.computePlayerAssets(game, player);
            if (playersAssets >= rent) {
                // TODO: send message with proposal to sell something
                // TODO: if rent > 90 % of assets - propose to give up
            } else {
                // TODO: auto-bankruptcy
            }
            gameHelper.endTurn(game); // TODO: remove on logic edit
        }
    }
}