package com.monopolynew.service.impl;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.WebsocketEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.ChanceContainer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChanceContainerImpl implements ChanceContainer {

    @Getter
    private final List<Function<Game, List<WebsocketEvent>>> chances = List.of(
            moneyChance(50, true, "%s found $%s on the pavement"),
            moneyChance(70, true, "%s won $%s in the lottery"),
            moneyChance(10, true, "%s won last place in a beauty contest and got $ %s"),
            moneyChance(100, true, "%s received $%s due to the bank error"),
            moneyChance(50, true, "%s received $%s dividend"),

            moneyChance(30, false, "%s lost $%s somewhere"),
            moneyChance(50, false, "%s pays $%s for medical services"),

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
                return List.of(
                        SystemMessageEvent.text(
                                String.format("%s has a birthday - every player pays him(her) $ %s",
                                        currentPlayer.getName(), giftSize)),
                        new MoneyChangeEvent(moneyStates)
                );
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
                return List.of(
                        SystemMessageEvent.text(
                                String.format("%s pays everyone $%s for help with election campaign",
                                        currentPlayer.getName(), reward)),
                        new MoneyChangeEvent(moneyStates)
                );
            },

            game -> {
                Player currentPlayer = game.getCurrentPlayer();
                int perHouse = 40;
                int perHotel = 115;
                int tax = 0;
                List<Integer> housesOnStreets = Arrays.stream(game.getCurrentMap().getFields())
                        .filter(field -> field instanceof StreetField)
                        .map(field -> (StreetField) field)
                        .filter(field -> field.getOwner().equals(currentPlayer))
                        .map(StreetField::getHouses)
                        .filter(houses -> houses > 0)
                        .collect(Collectors.toList());
                for (Integer houses : housesOnStreets) {
                    tax += houses == Rules.MAX_HOUSES_ON_STREET ? perHotel : houses * perHouse;
                }

                List<WebsocketEvent> events = new ArrayList<>();
                events.add(SystemMessageEvent.text(String.format("%s failed tax audit and pays $%s per house and $%s per hotel owned", currentPlayer.getName(), perHouse, perHotel)));

                if (tax > 0) {
                    currentPlayer.takeMoney(tax);
                    events.add(new MoneyChangeEvent(Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                }
                return events;
            }
    );

    private Function<Game, List<WebsocketEvent>> skipTurnsChance(int turns, String formatMessage) {
        return game -> {
            Player currentPlayer = game.getCurrentPlayer();
            currentPlayer.skipTurns(turns);
            return Collections.singletonList(
                    SystemMessageEvent.text(
                            String.format(formatMessage, currentPlayer.getName(), turns))
            );
        };
    }

    private Function<Game, List<WebsocketEvent>> moneyChance(int amount, boolean give, String formatMessage) {
        return game -> {
            Player currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            return List.of(
                    SystemMessageEvent.text(
                            String.format(formatMessage, currentPlayer.getName(), amount)),
                    new MoneyChangeEvent(Collections.singletonList(MoneyState.fromPlayer(currentPlayer)))
            );
        };
    }
}