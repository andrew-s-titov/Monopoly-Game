package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.ChanceContainer;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameMessageExchanger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ChanceContainerImpl implements ChanceContainer {

    private final GameHelper gameHelper;
    private final GameMessageExchanger gameMessageExchanger;

    @Getter
    private final List<Consumer<Game>> chances = List.of(
            moneyChance(50, true, "%s found $%s on the pavement"),
            moneyChance(70, true, "%s won $%s in the lottery"),
            moneyChance(10, true, "%s won last place in a beauty contest and got $ %s"),
            moneyChance(100, true, "%s received $%s due to a bank error"),
            moneyChance(50, true, "%s received $%s dividend"),

            moneyChance(30, false, "%s lost $%s somewhere"),
            moneyChance(50, false, "%s payed $%s for medical services"),

            skipTurnsChance(1, "%s got ill and is skipping %s turn(s)"),
            skipTurnsChance(2, "%s got hit by a car and is skipping %s turn(s)"),

            game -> {
                var currentPlayer = game.getCurrentPlayer();
                var moneyStates = new ArrayList<MoneyState>();
                var players = game.getPlayers();
                int giftSize = 15;
                int giftTotal = 0;
                for (Player player : players) {
                    if (!player.equals(currentPlayer)) {
                        giftTotal += player.takeMoney(giftSize);
                        moneyStates.add(MoneyState.fromPlayer(player));
                    }
                }
                currentPlayer.addMoney(giftTotal);
                moneyStates.add(MoneyState.fromPlayer(currentPlayer));
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s has a birthday - every player is paying him(her) $%s",
                                currentPlayer.getName(), giftSize)));
                gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
                gameHelper.endTurn(game);
            },

            game -> {
                var currentPlayer = game.getCurrentPlayer();
                int playerMoney = currentPlayer.getMoney();
                var moneyStates = new ArrayList<MoneyState>();
                var players = game.getPlayers();
                int beneficiaryCount = players.size() - 1;
                int rewardRate = 25;
                int reward = (playerMoney >= rewardRate * beneficiaryCount) ? rewardRate : playerMoney / beneficiaryCount;

                for (Player otherPlayer : players) {
                    if (!otherPlayer.equals(currentPlayer)) {
                        otherPlayer.addMoney(reward);
                        currentPlayer.takeMoney(reward);
                        moneyStates.add(MoneyState.fromPlayer(otherPlayer));
                    }
                }
                moneyStates.add(MoneyState.fromPlayer(currentPlayer));
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s is paying everyone $%s for help with election campaign",
                                currentPlayer.getName(), reward)));
                gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
                gameHelper.endTurn(game);
            },

            game -> {
                var currentPlayer = game.getCurrentPlayer();
                int perHouse = 40;
                int perHotel = 115;
                int tax = 0;
                List<Integer> housesOnStreets = Arrays.stream(game.getGameMap().getFields())
                        .filter(field -> field instanceof StreetField)
                        .map(field -> (StreetField) field)
                        .filter(field -> field.getOwner().equals(currentPlayer))
                        .map(StreetField::getHouses)
                        .filter(houses -> houses > 0)
                        .collect(Collectors.toList());
                for (Integer houses : housesOnStreets) {
                    tax += houses == Rules.MAX_HOUSES_ON_STREET ? perHotel : houses * perHouse;
                }
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s failed tax audit and is paying $%s per house and $%s per hotel owned",
                                currentPlayer.getName(), perHouse, perHotel)));
                if (tax > 0) {
                    currentPlayer.takeMoney(tax);
                    gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(
                            Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                }
                gameHelper.endTurn(game);
            },

            game -> {
                var currentPlayer = game.getCurrentPlayer();
                gameHelper.sendToJailAndEndTurn(game, currentPlayer, "for bribing a traffic police officer");
            }
    );

    private Consumer<Game> skipTurnsChance(int turns, String formatMessage) {
        return game -> {
            Player currentPlayer = game.getCurrentPlayer();
            currentPlayer.skipTurns(turns);
            gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                    String.format(formatMessage, currentPlayer.getName(), turns)));
            gameHelper.endTurn(game);
        };
    }

    private Consumer<Game> moneyChance(int amount, boolean give, String formatMessage) {
        return game -> {
            Player currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                    String.format(formatMessage, currentPlayer.getName(), amount)));
            gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            gameHelper.endTurn(game);
        };
    }
}