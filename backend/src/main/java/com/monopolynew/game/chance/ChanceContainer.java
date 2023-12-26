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
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
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
import static com.monopolynew.map.PurchasableFieldGroups.UTILITY_FIELD_GROUP;

@Component
public class ChanceContainer {

    private static final String YOU = "You";

    @Getter
    private final List<ChanceCard> chances;

    private final GameLogicExecutor gameLogicExecutor;
    private final GameEventSender gameEventSender;

    private final Random random = new Random();

    @Autowired
    public ChanceContainer(GameLogicExecutor gameLogicExecutor, GameEventSender gameEventSender) {
        this.gameLogicExecutor = gameLogicExecutor;
        this.gameEventSender = gameEventSender;
        this.chances = initializeChances();
    }

    private List<ChanceCard> initializeChances() {
        return List.of(
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

                advanceToNearest(COMPANY_FIELD_GROUP,
                        "%s missed the train on business trip and must proceed to the nearest Airport",
                        null),
                advanceToNearest(UTILITY_FIELD_GROUP,
                        "%s must proceed to the nearest Utility and pay full price for overdue payment",
                        game -> game.setLastDice(new DiceResult(6, 6))),

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
    }

    private ChanceCard skipTurnsChance(int turns, String messageTemplate) {
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
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), turns);
            var cardMessage = messageTemplate.formatted(YOU, turns);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.endTurn(game);
        };
    }

    private ChanceCard moneyChance(int amount, boolean give, String messageTemplate) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            if (give) {
                currentPlayer.addMoney(amount);
            } else {
                currentPlayer.takeMoney(amount);
            }
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), amount);
            var cardMessage = messageTemplate.formatted(YOU, amount);
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.endTurn(game);
        };
    }

    private ChanceCard everyonePays(int payment, String reason) {
        return game -> {
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
            var messageTemplate = "%s received $%s from every player %s";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), payment, reason);
            var personalMessage = messageTemplate.formatted(YOU, payment, reason);
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(personalMessage));
            gameLogicExecutor.endTurn(game);
        };
    }

    private ChanceCard payEveryoneForElection() {
        return game -> {
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
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(moneyStates));
            var messageTemplate = "%s must pay everyone $%s for help with the election campaign";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), rewardRate);
            var cardMessage = messageTemplate.formatted(YOU, rewardRate);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.endTurn(game);
        };
    }

    private ChanceCard payForEachBuilding(int perHouse, int perHotel, String reason) {
        return game -> {
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
                gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                        Collections.singletonList(MoneyState.fromPlayer(currentPlayer))));
            }
            var messageTemplate = "%s must pay $%s per house and $%s per hotel owned %s";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), perHouse, perHotel, reason);
            var cardMessage = messageTemplate.formatted(YOU, perHouse, perHotel, reason);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.endTurn(game);
        };
    }

    private ChanceCard advanceToNearest(int fieldGroup, String messageTemplate, Consumer<Game> additionalProcessing) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            int currentPosition = currentPlayer.getPosition();
            int whereTo = PurchasableFieldGroups.getGroupById(game, fieldGroup).stream()
                    .map(field -> {
                        int fieldPosition = field.getId();
                        int positionComputation = fieldPosition - currentPosition;
                        var steps = positionComputation < 0 ?
                                positionComputation + Rules.NUMBER_OF_FIELDS :
                                positionComputation;
                        return Pair.of(fieldPosition, steps);
                    })
                    .min(Comparator.comparingInt(Pair::getRight))
                    .orElseThrow(() -> new IllegalStateException("wrong distance calculations"))
                    .getLeft();
            if (additionalProcessing != null) {
                additionalProcessing.accept(game);
            }
            var chatMessage = messageTemplate.formatted(currentPlayer.getName());
            var cardMessage = messageTemplate.formatted(YOU);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.movePlayer(game, currentPlayer, whereTo, true);
        };
    }

    private ChanceCard advanceToPurchasableField(int fieldIndex, String reason) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            String fieldName = ((PurchasableField) game.getGameMap().getField(fieldIndex)).getName();
            var messageTemplate = "%s must advance to %s %s";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), fieldName, reason);
            var cardMessage = messageTemplate.formatted(YOU, fieldName, reason);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.movePlayer(game, currentPlayer, fieldIndex, true);
        };
    }

    private ChanceCard goToStartAfterBooze() {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            currentPlayer.skipTurns(1);
            if (game.getLastDice().isDoublet()) {
                currentPlayer.resetDoublets();
            }
            var messageTemplate = "%s got drunk and landed on the Start field, missing 1 turn";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName());
            var cardMessage = messageTemplate.formatted(YOU);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.movePlayer(game, currentPlayer, 0, false);
        };
    }

    private ChanceCard goThreeStepsBack() {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            int positionComputation = currentPlayer.getPosition() - 3;
            int newPosition = positionComputation < 0
                    ? positionComputation + Rules.NUMBER_OF_FIELDS
                    : positionComputation;
            var messageTemplate = "%s forgot a card inside an ATM and must go back for 3 fields";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName());
            var cardMessage = messageTemplate.formatted(YOU);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.movePlayer(game, currentPlayer, newPosition, false);
        };
    }

    private ChanceCard teleport() {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            GameMap gameMap = game.getGameMap();
            List<Integer> purchasableFieldsIndexes = gameMap.getFields().stream()
                    .filter(PurchasableField.class::isInstance)
                    .map(GameField::getId)
                    .toList();
            var randomArrayIndex = random.nextInt(purchasableFieldsIndexes.size());
            Integer randomFieldIndex = purchasableFieldsIndexes.get(randomArrayIndex);
            var messageTemplate = "%s got kidnapped and teleported by aliens";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName());
            var cardMessage = messageTemplate.formatted(YOU);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.movePlayer(game, currentPlayer, randomFieldIndex, false);
        };
    }

    private ChanceCard goToJail(String reason) {
        return game -> {
            var currentPlayer = game.getCurrentPlayer();
            var messageTemplate = "%s got sent to jail %s";
            var chatMessage = messageTemplate.formatted(currentPlayer.getName(), reason);
            var cardMessage = messageTemplate.formatted(YOU, reason);
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(chatMessage));
            gameEventSender.sendToPlayer(currentPlayer.getId(), new ChanceCardEvent(cardMessage));
            gameLogicExecutor.sendToJailAndEndTurn(game, currentPlayer);
        };
    }
}