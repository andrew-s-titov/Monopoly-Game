package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.StepProcessor;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
            Map<Integer, List<PurchasableField>> groups = game.getGameMap().getGroups();
            int rent;
            if (field instanceof StreetField) {
                rent = getStreetFieldRent((StreetField) field, groups);
            } else if (field instanceof CompanyField) {
                rent = getCompanyFieldRent((CompanyField) field, groups);
            } else if (field instanceof UtilityField) {
                rent = getUtilityFieldRent((UtilityField) field, groups, game.getLastDice());
            } else {
                throw new IllegalStateException("Failed to compute fair - unknown field type");
            }
            processRentPayment(game, player, field, rent);
        }
    }

    private int getStreetFieldRent(StreetField streetField, Map<Integer, List<PurchasableField>> groups) {
        long groupOwners = groups.get(streetField.getGroup()).stream()
                .map(PurchasableField::getOwner)
                .distinct().count();
        return streetField.computeRent(groupOwners == 1);
    }

    private int getCompanyFieldRent(CompanyField companyField, Map<Integer, List<PurchasableField>> groups) {
        int ownedByTheSameOwner = (int) groups.get(companyField.getGroup()).stream()
                .filter(f -> f.getOwner().equals(companyField.getOwner()))
                .count();
        return companyField.computeFare(ownedByTheSameOwner);
    }

    private int getUtilityFieldRent(UtilityField utilityField, Map<Integer, List<PurchasableField>> groups, DiceResult diceResult) {
        long groupOwners = groups.get(utilityField.getGroup()).stream()
                .map(PurchasableField::getOwner)
                .distinct().count();
        return utilityField.computeFare(diceResult, groupOwners == 1);
    }

    private boolean isRentPaymentNeeded(Game game, Player player, PurchasableField field) {
        if (field.isMortgaged()) {
            gameHelper.endTurn(game);
            return false;
        } else if (field.isFree()) {
            int streetPrice = field.getPrice();
            if (streetPrice <= player.getMoney()) {
                gameHelper.sendBuyProposal(game, player, field);
            } else {
                // TODO: auto auction or let sell or pledge something?
                auctionManager.startNewAuction(game, field);
            }
            return false;
        }
        return true;
    }

    private void processRentPayment(Game game, Player player, PurchasableField field, int rent) {
        int playerMoney = player.getMoney();
        Player owner = field.getOwner();
        if (playerMoney >= rent) {
            player.takeMoney(rent);
            owner.addMoney(rent);
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                    String.format("%s is moving to %s and paying %s $%s rent",
                            player.getName(), field.getName(), owner.getName(), rent)));
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
