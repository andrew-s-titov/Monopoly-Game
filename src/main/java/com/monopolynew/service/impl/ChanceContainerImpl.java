package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.FieldAction;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.service.ChanceContainer;
import com.monopolynew.service.GameLogicExecutor;
import com.monopolynew.service.GameEventSender;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.monopolynew.map.PurchasableFieldGroups.COMPANY_FIELD_GROUP;

@Component
public class ChanceContainerImpl implements ChanceContainer {

    @Getter
    private final List<Consumer<Game>> chances;

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;

    @Autowired
    public ChanceContainerImpl(GameLogicExecutor gameLogicExecutor, GameEventSender gameEventSender) {
        this.gameLogicExecutor = gameLogicExecutor;
        this.gameEventSender = gameEventSender;
        this.chances = createChances(gameLogicExecutor, gameEventSender);
    }

    private List<Consumer<Game>> createChances(GameLogicExecutor gameLogicExecutor, GameEventSender gameEventSender) {
        return List.of(
                moneyChance(50, true, "%s found $%s on the pavement"),
                moneyChance(70, true, "%s won $%s in the lottery"),
                moneyChance(10, true, "%s won last place in a beauty contest and got $ %s"),
                moneyChance(100, true, "%s received $%s due to a bank error"),
                moneyChance(50, true, "%s received $%s dividend"),
                moneyChance(30, true, "%s won $%s in a casino"),
                moneyChance(100, true, "%s received $%s from an unknown admirer"),
                moneyChance(20, true, "%s received $%s income tax refund "),

                moneyChance(30, false, "%s lost $%s somewhere"),
                moneyChance(50, false, "%s payed $%s for medical services"),
                moneyChance(50, false, "%s payed $%s for additional education"),
                moneyChance(40, false, "%s lost $%s in a casino"),

                skipTurnsChance(1, "%s got ill and is skipping %s turn(s)"),
                skipTurnsChance(2, "%s got hit by a car and is skipping %s turn(s)"),

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    var moneyStates = new ArrayList<MoneyState>();
                    var players = game.getPlayers();
                    int giftSize = 15;
                    int giftTotal = 0;
                    for (Player otherPlayer : players) {
                        if (!otherPlayer.equals(currentPlayer) && !otherPlayer.isBankrupt()) {
                            giftTotal += otherPlayer.takeMoney(giftSize);
                            moneyStates.add(MoneyState.fromPlayer(otherPlayer));
                        }
                    }
                    currentPlayer.addMoney(giftTotal);
                    moneyStates.add(MoneyState.fromPlayer(currentPlayer));
                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                            String.format("%s has a birthday - every player is paying him/her $%s",
                                    currentPlayer.getName(), giftSize)));
                    gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
                    gameLogicExecutor.endTurn(game);
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
                        if (!otherPlayer.equals(currentPlayer) && !otherPlayer.isBankrupt()) {
                            otherPlayer.addMoney(reward);
                            currentPlayer.takeMoney(reward);
                            moneyStates.add(MoneyState.fromPlayer(otherPlayer));
                        }
                    }
                    moneyStates.add(MoneyState.fromPlayer(currentPlayer));
                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                            String.format("%s is paying everyone $%s for help with election campaign",
                                    currentPlayer.getName(), rewardRate)));
                    gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
                    gameLogicExecutor.endTurn(game);
                },

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    int perHouse = 40;
                    int perHotel = 115;
                    int tax = 0;
                    List<Integer> housesOnStreets = game.getGameMap().getFields().stream()
                            .filter(field -> field instanceof StreetField)
                            .map(field -> (StreetField) field)
                            .filter(field -> !field.isFree())
                            .filter(field -> field.getOwner().equals(currentPlayer))
                            .map(StreetField::getHouses)
                            .filter(houses -> houses > 0)
                            .collect(Collectors.toList());
                    for (Integer houses : housesOnStreets) {
                        tax += houses == Rules.MAX_HOUSES_ON_STREET ? perHotel : houses * perHouse;
                    }
                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                            String.format("%s failed tax audit and is paying $%s per house and $%s per hotel owned",
                                    currentPlayer.getName(), perHouse, perHotel)));
                    if (tax > 0) {
                        currentPlayer.takeMoney(tax);
                        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                                Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
                    }
                    gameLogicExecutor.endTurn(game);
                },

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    gameLogicExecutor.sendToJailAndEndTurn(game, currentPlayer, "for bribing a traffic police officer");
                },

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    int currentPosition = currentPlayer.getPosition();
                    CompanyField nearestField = PurchasableFieldGroups.getGroup(game, COMPANY_FIELD_GROUP).stream()
                            .map(field -> (CompanyField) field)
                            .map(field -> {
                                int fieldPosition = field.getId();
                                int forward = (fieldPosition > currentPosition) ?
                                        fieldPosition - currentPosition :
                                        currentPosition + Rules.NUMBER_OF_FIELDS - fieldPosition;
                                int backward = (fieldPosition > currentPosition) ?
                                        fieldPosition + Rules.NUMBER_OF_FIELDS - currentPosition :
                                        currentPosition - fieldPosition;
                                return Pair.of(field, Math.min(forward, backward));
                            })
                            .min(Comparator.comparingInt(Pair::getValue))
                            .orElseThrow(() -> new IllegalStateException("wrong distance calculations"))
                            .getKey();

                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(currentPlayer.getName()
                            + " was urgently called on a business trip and is proceeding to the nearest airport"));
                    gameLogicExecutor.movePlayer(game, currentPlayer, nearestField.getId(), false);
                },

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                            String.format("%s unexpectedly ended up on the %s field after a booze",
                                    currentPlayer.getName(), FieldAction.START.getName())));
                    boolean forward = currentPlayer.getPosition() > Rules.NUMBER_OF_FIELDS / 2;
                    gameLogicExecutor.movePlayer(game, currentPlayer, 0, forward);
                },

                game -> {
                    var currentPlayer = game.getCurrentPlayer();
                    gameEventSender.sendToAllPlayers(new SystemMessageEvent(currentPlayer.getName() + " is TELEPORTING!"));
                    GameMap gameMap = game.getGameMap();
                    List<Integer> purchasableFieldsIndexes = gameMap.getFields().stream()
                            .filter(field -> field instanceof PurchasableField)
                            .map(GameField::getId)
                            .collect(Collectors.toList());
                    var random = new Random().nextInt(purchasableFieldsIndexes.size());
                    Integer randomFieldIndex = purchasableFieldsIndexes.get(random);
                    gameLogicExecutor.movePlayer(game, currentPlayer, randomFieldIndex, false);
                }
        );
    }

    private Consumer<Game> skipTurnsChance(int turns, String formatMessage) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            var lastDice = game.getLastDice();
            if (lastDice != null && lastDice.isDoublet()) {
                if (turns > 1) {
                    currentPlayer.skipTurns(turns - 1);
                }
                currentPlayer.resetDoublets();
            } else {
                currentPlayer.skipTurns(turns);
            }
            gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                    String.format(formatMessage, currentPlayer.getName(), turns)));
            gameLogicExecutor.endTurn(game);
        };
    }

    private Consumer<Game> moneyChance(int amount, boolean give, String formatMessage) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                    String.format(formatMessage, currentPlayer.getName(), amount)));
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            gameLogicExecutor.endTurn(game);
        };
    }
}