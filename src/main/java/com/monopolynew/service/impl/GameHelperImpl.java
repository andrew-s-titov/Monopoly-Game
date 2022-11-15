package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.StreetNewOwnerChange;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.PropertyNewOwnerChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameMessageExchanger;
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

    private final GameMessageExchanger gameMessageExchanger;

    @Override
    public int movePlayer(Game game, Player player) {
        int currentPosition = player.getPosition();
        int result = currentPosition + game.getLastDice().getSum();
        boolean newCircle = result > GameMap.LAST_FIELD_INDEX;
        int newPosition = newCircle ? result - GameMap.LAST_FIELD_INDEX - 1 : result;
        changePlayerPosition(player, newPosition);
        if (newCircle) {
            player.addMoney(Rules.CIRCLE_MONEY);
            if (newPosition == 0) {
                player.addMoney(Rules.CIRCLE_MONEY);
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s receives $%sk for hitting %s field",
                                player.getName(), Rules.CIRCLE_MONEY * 2, game.getCurrentMap().getField(0).getName())));
            } else {
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s receives $%sk for starting a new circle",
                                player.getName(), Rules.CIRCLE_MONEY)));
            }
            gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }
        return newPosition;
    }

    @Override
    public void sendToJail(Game game, Player player, @Nullable String reason) {
        player.resetDoublets();
        player.imprison();
        gameMessageExchanger.sendToAllPlayers(
                SystemMessageEvent.text(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, GameMap.JAIL_FIELD_NUMBER);
        endTurn(game);
    }

    @Override
    public void doBuyField(PurchasableField field, int price, Player player) {
        field.newOwner(player);
        player.takeMoney(price);
        gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s buys %s for $%sk", player.getName(), field.getName(), price)));
        gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(player))));
        gameMessageExchanger.sendToAllPlayers(new PropertyNewOwnerChangeEvent(
                Collections.singletonList(new StreetNewOwnerChange(player.getId(), field.getId()))));
        // TODO: check if player now owns all group, send event for price change for every field
    }

    @Override
    public void endTurn(Game game) {
        processMortgage(game);
        Player currentPlayer = game.getCurrentPlayer();
        Player nextPlayer = game.getLastDice().isDoublet() && !currentPlayer.isSkipping() && !currentPlayer.isImprisoned() ?
                currentPlayer : toNextPlayer(game);
        if (nextPlayer.isImprisoned()) {
            game.setStage(GameStage.JAIL_RELEASE);
            String playerId = nextPlayer.getId();
            gameMessageExchanger.sendToPlayer(playerId,
                    new JailReleaseProcessEvent(playerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            gameMessageExchanger.sendToAllPlayers(TurnStartEvent.forPlayer(nextPlayer));
        }
    }

    private void checkPlayerCanMakeMove(Player player) {
        if (player.isSkipping()) {
            throw new IllegalStateException("skipping player cannot do regular turn");
        }
        if (player.isImprisoned()) {
            throw new IllegalStateException("imprisoned player cannot do regular turn");
        }
    }

    private void changePlayerPosition(Player player, int fieldId) {
        player.changePosition(fieldId);
        gameMessageExchanger.sendToAllPlayers(new ChipMoveEvent(player.getId(), fieldId));
    }

    @Override
    public int computePlayerAssets(Game game, Player player) {
        int assetSum = player.getMoney();
        List<PurchasableField> playerFields = Arrays.stream(game.getCurrentMap().getFields())
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

    private Player toNextPlayer(Game game) {
        Player nextPlayer = game.nextPlayer();
        if (nextPlayer.isSkipping()) {
            gameMessageExchanger.sendToAllPlayers(
                    SystemMessageEvent.text(nextPlayer.getName() + " is skipping his turn"));
            nextPlayer.skip();
            processMortgage(game);
            nextPlayer = toNextPlayer(game);
        }
        return nextPlayer;
    }

    private void processMortgage(Game game) {
        Arrays.stream(game.getCurrentMap().getFields())
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
