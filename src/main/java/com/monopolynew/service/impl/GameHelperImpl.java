package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.StreetNewOwnerChange;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.PropertyNewOwnerChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.FieldAction;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class GameHelperImpl implements GameHelper {

    private final GameEventSender gameEventSender;

    @Override
    public void movePlayerForward(Game game, Player player, int newPosition) {
        int currentPosition = player.getPosition();
        boolean newCircle = newPosition < currentPosition;
        changePlayerPosition(player, newPosition);
        if (newCircle) {
            player.addMoney(Rules.CIRCLE_MONEY);
            if (newPosition == 0) {
                player.addMoney(Rules.CIRCLE_MONEY);
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s received $%s for hitting %s field",
                                player.getName(), Rules.CIRCLE_MONEY * 2, FieldAction.START.getName())));
            } else {
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s received $%s for starting a new circle",
                                player.getName(), Rules.CIRCLE_MONEY)));
            }
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }
    }

    @Override
    public void changePlayerPosition(Player player, int fieldId) {
        player.changePosition(fieldId);
        gameEventSender.sendToAllPlayers(new ChipMoveEvent(player.getId(), fieldId));
    }

    @Override
    public void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason) {
        player.resetDoublets();
        player.imprison();
        gameEventSender.sendToAllPlayers(
                SystemMessageEvent.text(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, GameMap.JAIL_FIELD_NUMBER);
        endTurn(game);
    }

    @Override
    public void sendBuyProposal(Game game, Player player, PurchasableField field) {
        var buyProposal = new BuyProposal(player, field);
        game.setBuyProposal(buyProposal);
        game.setStage(GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(player.getId(), BuyProposalEvent.fromProposal(buyProposal));
    }

    @Override
    public void doBuyField(PurchasableField field, int price, Player player) {
        field.newOwner(player);
        player.takeMoney(price);
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s is buying %s for $%s", player.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(player))));
        gameEventSender.sendToAllPlayers(new PropertyNewOwnerChangeEvent(
                Collections.singletonList(new StreetNewOwnerChange(player.getId(), field.getId()))));

        // TODO: check if player now owns all group, send event for price change for every field
    }

    @Override
    public int computePlayerAssets(Game game, Player player) {
        int assetSum = player.getMoney();
        List<PurchasableField> playerFields = Arrays.stream(game.getGameMap().getFields())
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(field -> field.getOwner().equals(player))
                .collect(Collectors.toList());
        for (PurchasableField field : playerFields) {
            if (field instanceof StreetField) {
                int houses = ((StreetField) field).getHouses();
                int housePrice = ((StreetField) field).getHousePrice();
                assetSum += housePrice * houses;
            }
            int price = field.getPrice();
            assetSum += price / 2;
        }
        return assetSum;
    }

    @Override
    public void endTurn(Game game) {
        processMortgage(game);
        Player currentPlayer = game.getCurrentPlayer();
        Player nextPlayer = (game.getLastDice().isDoublet() && !currentPlayer.isSkipping() && !currentPlayer.isImprisoned()) ?
                currentPlayer : toNextPlayer(game);
        if (nextPlayer.isImprisoned()) {
            game.setStage(GameStage.JAIL_RELEASE);
            String playerId = nextPlayer.getId();
            gameEventSender.sendToPlayer(playerId,
                    new JailReleaseProcessEvent(playerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(nextPlayer));
        }
    }

    private Player toNextPlayer(Game game) {
        Player nextPlayer = game.nextPlayer();
        if (nextPlayer.isSkipping()) {
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(nextPlayer.getName() + " is skipping his turn"));
            nextPlayer.skip();
            processMortgage(game);
            nextPlayer = toNextPlayer(game);
        }
        return nextPlayer;
    }

    private void processMortgage(Game game) {
        Arrays.stream(game.getGameMap().getFields())
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(PurchasableField::isMortgaged)
                .forEach(field -> {
                    int mortgageTurns = (field).decreaseMortgageTurns();
                    // TODO: send mortgage change event
                    if (mortgageTurns == 0) {
                        // TODO: send owner change event (or 'mortgage release');
                    }
                });
        // TODO: better send in one 'batch' to have all-or-non scheme
    }
}