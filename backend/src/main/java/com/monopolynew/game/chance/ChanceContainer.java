package com.monopolynew.game.chance;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.ChanceCardEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameEventSender;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.monopolynew.map.PurchasableFieldGroups.AIRPORT_FIELD_GROUP;
import static com.monopolynew.map.PurchasableFieldGroups.UTILITY_FIELD_GROUP;

@UtilityClass
public class ChanceContainer {

    private static final String YOU = "You";

    public static final List<ChanceCard> CHANCES = List.of(
            moneyChance(50, true, "chance.money.get.pavement"),
            moneyChance(70, true, "chance.money.get.lottery"),
            moneyChance(9, true, "chance.money.get.contest"),
            moneyChance(100, true, "chance.money.get.error"),
            moneyChance(50, true, "chance.money.get.dividend"),
            moneyChance(30, true, "chance.money.get.casino"),
            moneyChance(80, true, "chance.money.get.admirer"),
            moneyChance(20, true, "chance.money.get.refund"),
            moneyChance(50, true, "chance.money.get.stock"),
            moneyChance(25, true, "chance.money.get.fee"),
            moneyChance(150, true, "chance.money.get.inheritance"),
            moneyChance(70, true, "chance.money.get.insurance"),
            moneyChance(70, true, "chance.money.get.donation"),

            moneyChance(30, false, "chance.money.give.lost"),
            moneyChance(50, false, "chance.money.give.medicine"),
            moneyChance(50, false, "chance.money.give.education"),
            moneyChance(40, false, "chance.money.give.casino"),
            moneyChance(15, false, "chance.money.give.fine"),
            moneyChance(65, false, "chance.money.give.repairs"),
            moneyChance(50, false, "chance.money.give.bet"),

            skipTurnsChance(1, "chance.skip.ill"),
            skipTurnsChance(2, "chance.skip.accident"),

            everyonePays(15, "chance.everyonePays.birthday"),
            everyonePays(50, "chance.everyonePays.party"),
            everyonePays(10, "chance.everyonePays.concert"),

            payEveryoneForElection(),

            payForEachBuilding(40, 115, "chance.payForProperty.audit"),
            payForEachBuilding(25, 100, "chance.payForProperty.repair"),

            goToStartAfterBooze(),
            goThreeStepsBack(),
            teleport(),

            advanceToAirport(),
            advanceToUtility(),

            advanceToPurchasableField(39, "chance.goTo.london"),
            advanceToPurchasableField(11, "chance.goTo.budapest"),
            advanceToPurchasableField(24, "chance.goTo.athens"),
            advanceToPurchasableField(31, "chance.goTo.luxembourg"),

            goToJail("chance.jail.drunk"),
            goToJail("chance.jail.bribe")
    );

    private static ChanceCard skipTurnsChance(int turns, String translationKey) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            skipTurns(turns, currentPlayer);
            sendChanceMessages(game, eventSender,
                    new SystemMessageEvent(translationKey, Map.of(
                            "name", currentPlayer.getName(),
                            "turns", turns)),
                    translationKey.formatted(YOU, turns));
            return null;
        };
    }

    private static ChanceCard moneyChance(int amount, boolean give, String translationKey) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            sendChanceMessages(game, eventSender,
                    new SystemMessageEvent(translationKey, Map.of(
                            "name", currentPlayer.getName(),
                            "amount", amount)),
                    translationKey);
            eventSender.sendToAllPlayers(game.getId(), new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            return null;
        };
    }

    private static ChanceCard everyonePays(int payment, String translationKey) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            var payers = game.getPlayers().stream()
                    .filter(player -> !player.equals(currentPlayer) && !player.isBankrupt())
                    .toList();
            var giftTotal = payers.stream()
                    .map(payer -> payer.takeMoney(payment))
                    .reduce(0, Integer::sum);
            currentPlayer.addMoney(giftTotal);
            var moneyStates = payers.stream()
                    .map(MoneyState::fromPlayer)
                    .collect(Collectors.toCollection(ArrayList::new));
            moneyStates.add(MoneyState.fromPlayer(currentPlayer));
            sendChanceMessages(game, eventSender,
                    new SystemMessageEvent(translationKey, Map.of(
                            "name", currentPlayer.getName(),
                            "amount", payment)),
                    translationKey);
            eventSender.sendToAllPlayers(game.getId(), new MoneyChangeEvent(moneyStates));
            return null;
        };
    }

    private static ChanceCard payEveryoneForElection() {
        return (game, eventSender) -> {
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
            eventSender.sendToAllPlayers(game.getId(), new MoneyChangeEvent(moneyStates));
            sendChanceMessages(game, eventSender,
                    new SystemMessageEvent("chance.payEveryone", Map.of(
                            "name", currentPlayer.getName(),
                            "amount", rewardRate)),
                    "chance.payEveryone");
            return null;
        };
    }

    private static ChanceCard payForEachBuilding(int perHouse, int perHotel, String translationKey) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            int tax = game.getGameMap().getFields().stream()
                    .filter(StreetField.class::isInstance)
                    .map(StreetField.class::cast)
                    .filter(field -> currentPlayer.equals(field.getOwner()))
                    .map(StreetField::getHouses)
                    .filter(houses -> houses > 0)
                    .map(houses -> houses == Rules.MAX_HOUSES_ON_STREET ? perHotel : houses * perHouse)
                    .reduce(0, Integer::sum);
            if (tax > 0) {
                currentPlayer.takeMoney(tax);
                eventSender.sendToAllPlayers(game.getId(), new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            }
            var systemMessage = new SystemMessageEvent(translationKey, Map.of(
                    "name", currentPlayer.getName(),
                    "perHouse", perHouse,
                    "perHotel", perHotel));
            sendChanceMessages(game, eventSender, systemMessage, translationKey);
            return null;
        };
    }

    private static ChanceCard advanceToUtility() {
        return (game, eventSender) -> {
            var whereTo = (UtilityField) nearestForward(game, UTILITY_FIELD_GROUP);

            Player currentPlayer = game.getCurrentPlayer();
            if (!whereTo.isFree() && !currentPlayer.equals(whereTo.getOwner())) {
                game.setLastDice(new DiceResult(12, 0));
            }
            var message = "%s must proceed to the nearest Utility and pay full price for overdue payment";
            return goTo(game, eventSender,
                    whereTo.getId(), true,
                    new SystemMessageEvent("chance.goTo.utility", Map.of("name", currentPlayer.getName())),
                    message.formatted(YOU));
        };
    }

    private static ChanceCard advanceToAirport() {
        return (game, eventSender) -> {
            var nearestForwardAirport = nearestForward(game, AIRPORT_FIELD_GROUP);
            var message = "%s missed the train on business trip and must proceed to the nearest Airport";
            return goTo(game, eventSender,
                    nearestForwardAirport.getId(), true,
                    new SystemMessageEvent("chance.goTo.airport", Map.of(
                            "name", game.getCurrentPlayer().getName())),
                    message.formatted(YOU));
        };
    }

    private static ChanceCard advanceToPurchasableField(int fieldIndex, String translationKey) {
        return (game, eventSender) -> {
            var field = (PurchasableField) game.getGameMap().getField(fieldIndex);
            String fieldName = field.getName();
            var message = "%s must advance to %s %s";
            return goTo(game, eventSender, field.getId(), true,
                    new SystemMessageEvent(translationKey, Map.of("name", game.getCurrentPlayer().getName())),
                    message.formatted(YOU, fieldName, translationKey));
        };
    }

    private static ChanceCard goToStartAfterBooze() {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            currentPlayer.skipTurns(1);
            if (currentPlayer.getDoubletCount() > 0) {
                currentPlayer.resetDoublets();
            }
            var messageTemplate = "%s got drunk and landed on the Start field, missing 1 turn";
            return goTo(game, eventSender, 0, false,
                    new SystemMessageEvent("chance.goTo.start", Map.of("name", currentPlayer.getName())),
                    messageTemplate.formatted(YOU));
        };
    }

    private static ChanceCard goThreeStepsBack() {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            int positionComputation = currentPlayer.getPosition() - 3;
            int newPosition = positionComputation < 0
                    ? positionComputation + Rules.NUMBER_OF_FIELDS
                    : positionComputation;
            var messageTemplate = "%s forgot a card inside an ATM and must go back for 3 fields";
            return goTo(game, eventSender,
                    newPosition, false,
                    new SystemMessageEvent("chance.threeStepsBack", Map.of(
                            "name", currentPlayer.getName())),
                    messageTemplate.formatted(YOU));
        };
    }

    private static ChanceCard teleport() {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            GameMap gameMap = game.getGameMap();
            List<Integer> purchasableFieldsIndexes = gameMap.getFields().stream()
                    .filter(PurchasableField.class::isInstance)
                    .map(GameField::getId)
                    .toList();
            var random = new Random();
            var randomArrayIndex = random.nextInt(purchasableFieldsIndexes.size());
            Integer randomFieldIndex = purchasableFieldsIndexes.get(randomArrayIndex);
            var messageTemplate = "%s got kidnapped and teleported by aliens";
            return goTo(game, eventSender,
                    randomFieldIndex, false,
                    new SystemMessageEvent("chance.teleport", Map.of(
                            "name", currentPlayer.getName())),
                    messageTemplate.formatted(YOU));
        };
    }

    private static ChanceCard goToJail(String translationKey) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            var messageTemplate = "%s got sent to jail %s";
            return goTo(game, eventSender, Rules.JAIL_FIELD_NUMBER, false,
                    new SystemMessageEvent(translationKey, Map.of("name", currentPlayer.getName())),
                    messageTemplate.formatted(YOU, translationKey));
        };
    }

    private static PurchasableField nearestForward(Game game, int fieldGroup) {
        var currentPlayer = game.getCurrentPlayer();
        int currentPosition = currentPlayer.getPosition();
        return PurchasableFieldGroups.getGroupById(game, fieldGroup).stream()
                .map(field -> {
                    int fieldPosition = field.getId();
                    int positionComputation = fieldPosition - currentPosition;
                    var steps = positionComputation < 0 ?
                            positionComputation + Rules.NUMBER_OF_FIELDS :
                            positionComputation;
                    return Pair.of(field, steps);
                })
                .min(Comparator.comparingInt(Pair::getRight))
                .orElseThrow(() -> new IllegalStateException("wrong distance calculations"))
                .getLeft();
    }

    private static GoTo goTo(Game game, GameEventSender eventSender,
                             int whereTo, boolean forward, SystemMessageEvent systemMessage, String cardMessage) {
        sendChanceMessages(game, eventSender, systemMessage, cardMessage);
        return new GoTo(whereTo, forward);
    }

    private static void sendChanceMessages(Game game, GameEventSender eventSender,
                                           SystemMessageEvent systemMessage, String cardMessage) {
        UUID gameId = game.getId();
        eventSender.sendToAllPlayers(gameId, systemMessage);
        eventSender.sendToPlayer(gameId, game.getCurrentPlayer().getId(), new ChanceCardEvent(cardMessage));
    }

    private static void skipTurns(int amount, Player currentPlayer) {
        if (currentPlayer.getDoubletCount() > 0) {
            currentPlayer.skipTurns(amount - 1);
            currentPlayer.resetDoublets();
        } else {
            currentPlayer.skipTurns(amount);
        }
    }
}