package com.monopolynew.game.chance;

import com.monopolynew.dto.MoneyState;
import com.monopolynew.event.ChanceCardEvent;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.MoneyChangeEvent;
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
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.monopolynew.map.PurchasableFieldGroups.AIRPORT_FIELD_GROUP;
import static com.monopolynew.map.PurchasableFieldGroups.UTILITY_FIELD_GROUP;

@UtilityClass
public class ChanceContainer {

    private static final String YOU = "You";

    public static final List<ChanceCard> CHANCES = List.of(
            moneyChance(50, true, "%s found $%s on the pavement"),
            moneyChance(70, true, "%s won $%s in the lottery"),
            moneyChance(9, true, "%s won last place in a beauty contest and got $%s"),
            moneyChance(100, true, "%s received $%s due to a bank error"),
            moneyChance(50, true, "%s received $%s dividend"),
            moneyChance(30, true, "%s won $%s in a casino"),
            moneyChance(80, true, "%s received $%s from an unknown admirer"),
            moneyChance(20, true, "%s received $%s income tax refund"),
            moneyChance(50, true, "%s received $%s from sale of stock"),
            moneyChance(25, true, "%s received $%s consultancy fee"),
            moneyChance(150, true, "%s inherited $%s"),
            moneyChance(70, true, "%s received $%s life insurance payment for injury"),
            moneyChance(70, true, "%s received $%s for blood donation"),

            moneyChance(30, false, "%s lost $%s somewhere"),
            moneyChance(50, false, "%s must pay $%s for medical services"),
            moneyChance(50, false, "%s must pay $%s for additional education"),
            moneyChance(40, false, "%s lost $%s in a casino"),
            moneyChance(15, false, "%s must pay $%s speeding fine"),
            moneyChance(65, false, "%s must pay $%s for car repairs"),
            moneyChance(50, false, "%s must pay $%s to a friend for a lost bet"),

            skipTurnsChance(1, "%s got ill and must skip %s turn"),
            skipTurnsChance(2, "%s got hit by a car and must skip %s turns"),

            everyonePays(15, "as a birthday present"),
            everyonePays(50, "for tickets to a private party"),
            everyonePays(10, "for a street music concert"),

            payEveryoneForElection(),

            payForEachBuilding(40, 115, "because of the failed tax audit"),
            payForEachBuilding(25, 100, "for general property repairs"),

            goToStartAfterBooze(),
            goThreeStepsBack(),
            teleport(),

            advanceToAirport(),
            advanceToUtility(),

            advanceToPurchasableField(39, "to visit Madame Tussauds Museum"),
            advanceToPurchasableField(11,
                    "to attend a conference in the European Institute of Innovation and Technology"),
            advanceToPurchasableField(24,
                    "to make a presentation in the European Union Agency for Cybersecurity"),
            advanceToPurchasableField(31,
                    "to participate in an antitrust lawsuit in the European Court of Justice"),

            goToJail("for drunken indecent behavior"),
            goToJail("for bribing a traffic police officer")
    );

    private static ChanceCard skipTurnsChance(int turns, String messageTemplate) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            skipTurns(turns, currentPlayer);
            sendChanceMessages(game, eventSender,
                    messageTemplate.formatted(currentPlayer.getName(), turns),
                    messageTemplate.formatted(YOU, turns));
            return null;
        };
    }

    private static ChanceCard moneyChance(int amount, boolean give, String messageTemplate) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            sendChanceMessages(game, eventSender,
                    messageTemplate.formatted(currentPlayer.getName(), amount),
                    messageTemplate.formatted(YOU, amount));
            eventSender.sendToAllPlayers(game.getId(), new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            return null;
        };
    }

    private static ChanceCard everyonePays(int payment, String reason) {
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
            var message = "%s received $%s from every player %s";
            sendChanceMessages(game, eventSender,
                    message.formatted(currentPlayer.getName(), payment, reason),
                    message.formatted(YOU, payment, reason));
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
            var message = "%s must pay everyone $%s for help with the election campaign";
            sendChanceMessages(game, eventSender,
                    message.formatted(currentPlayer.getName(), rewardRate),
                    message.formatted(YOU, rewardRate));
            return null;
        };
    }

    private static ChanceCard payForEachBuilding(int perHouse, int perHotel, String reason) {
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
            var message = "%s must pay $%s per house and $%s per hotel owned %s";
            sendChanceMessages(game, eventSender,
                    message.formatted(currentPlayer.getName(), perHouse, perHotel, reason),
                    message.formatted(YOU, perHouse, perHotel, reason));
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
                    message.formatted(currentPlayer.getName()),
                    message.formatted(YOU));
        };
    }

    private static ChanceCard advanceToAirport() {
        return (game, eventSender) -> {
            var nearestForwardAirport = nearestForward(game, AIRPORT_FIELD_GROUP);
            var message = "%s missed the train on business trip and must proceed to the nearest Airport";
            return goTo(game, eventSender,
                    nearestForwardAirport.getId(), true,
                    message.formatted(game.getCurrentPlayer().getName()),
                    message.formatted(YOU));
        };
    }

    private static ChanceCard advanceToPurchasableField(int fieldIndex, String reason) {
        return (game, eventSender) -> {
            var field = (PurchasableField) game.getGameMap().getField(fieldIndex);
            String fieldName = field.getName();
            var message = "%s must advance to %s %s";
            return goTo(game, eventSender, field.getId(), true,
                    message.formatted(game.getCurrentPlayer().getName(), fieldName, reason),
                    message.formatted(YOU, fieldName, reason));
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
                    messageTemplate.formatted(currentPlayer.getName()),
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
                    messageTemplate.formatted(currentPlayer.getName()),
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
                    messageTemplate.formatted(currentPlayer.getName()),
                    messageTemplate.formatted(YOU));
        };
    }

    private static ChanceCard goToJail(String reason) {
        return (game, eventSender) -> {
            var currentPlayer = game.getCurrentPlayer();
            var messageTemplate = "%s got sent to jail %s";
            return goTo(game, eventSender, Rules.JAIL_FIELD_NUMBER, false,
                    messageTemplate.formatted(currentPlayer.getName(), reason),
                    messageTemplate.formatted(YOU, reason));
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
                             int whereTo, boolean forward, String chatMessage, String cardMessage) {
        sendChanceMessages(game, eventSender, chatMessage, cardMessage);
        return new GoTo(whereTo, forward);
    }

    private static void sendChanceMessages(Game game, GameEventSender eventSender,
                                           String chatMessage, String cardMessage) {
        UUID gameId = game.getId();
        eventSender.sendToAllPlayers(gameId, new ChatMessageEvent(chatMessage));
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