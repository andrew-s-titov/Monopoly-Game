package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.DiceRollingStartEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Dice;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.ActionableField;
import com.monopolynew.map.FieldAction;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.ChanceExecutor;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.GameHolder;
import com.monopolynew.service.GameMapRefresher;
import com.monopolynew.service.GameService;
import com.monopolynew.service.StepProcessor;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class GameServiceImpl implements GameService {

    private final GameHolder gameHolder;
    private final Dice dice;
    private final GameHelper gameHelper;
    private final StepProcessor stepProcessor;
    private final AuctionManager auctionManager;
    private final ChanceExecutor chanceExecutor;
    private final GameEventSender gameEventSender;
    private final GameMapRefresher gameMapRefresher;

    @Override
    public boolean isGameStarted() {
        return gameHolder.getGame().isInProgress();
    }

    @Override
    public boolean usernameTaken(String username) {
        return gameHolder.getGame().isUsernameTaken(username);
    }

    @Override
    public String getPlayerName(String playerId) {
        return gameHolder.getGame().getPlayerById(playerId).getName();
    }

    @Override
    public void startGame() {
        Game game = gameHolder.getGame();
        Collection<Player> players = game.getPlayers();
        if (CollectionUtils.isEmpty(players)) {
            // TODO: conflict? not ready? what status to return?
            throw new IllegalStateException("No players registered - cannot start a game");
        }
        game.startGame();
        gameEventSender.sendToAllPlayers(gameMapRefresher.getRefreshEvent(game));
        Player currentPlayer = game.getCurrentPlayer();
        gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
    }

    @Override
    public void startRolling() {
        Game game = gameHolder.getGame();
        gameEventSender.sendToAllPlayers(new DiceRollingStartEvent(game.getCurrentPlayer().getId()));
    }

    @Override
    public void doRollTheDice() {
        Game game = gameHolder.getGame();
        var lastDice = dice.rollTheDice();
        game.setLastDice(lastDice);
        Player currentPlayer = game.getCurrentPlayer();
        String message = String.format("%s rolled the dice and got %s : %s",
                currentPlayer.getName(), lastDice.getFirstDice(), lastDice.getSecondDice());
        if (lastDice.isDoublet()) {
            message = message + " (doublet)";
            if (game.getStage().equals(GameStage.TURN_START)) {
                currentPlayer.incrementDoublets();
            }
        } else {
            currentPlayer.resetDoublets();
        }
        gameEventSender.sendToAllPlayers(
                new DiceResultEvent(currentPlayer.getId(), lastDice.getFirstDice(), lastDice.getSecondDice()));
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(message));
    }

    @Override
    public void afterDiceAction() {
        Game game = gameHolder.getGame();
        DiceResult lastDice = game.getLastDice();
        if (lastDice == null) {
            throw new IllegalStateException("throw dice must be called first");
        }
        Player currentPlayer = game.getCurrentPlayer();
        GameStage stage = game.getStage();
        if (GameStage.TURN_START.equals(stage)) {
            if (currentPlayer.committedFraud()) {
                currentPlayer.resetDoublets();
                gameHelper.sendToJailAndEndTurn(game, currentPlayer, "for fraud");
            } else {
                doRegularMove(game);
            }
        } else if (GameStage.JAIL_RELEASE.equals(stage)) {
            if (lastDice.isDoublet()) {
                currentPlayer.releaseFromJail();
                game.setStage(GameStage.TURN_START);
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        currentPlayer.getName() + " is pardoned under amnesty"));
                // auto move without action proposal
                doRegularMove(game);
            } else {
                if (currentPlayer.lastTurnInPrison()) {
                    if (currentPlayer.getMoney() >= Rules.JAIL_BAIL) {
                        currentPlayer.takeMoney(Rules.JAIL_BAIL);
                        currentPlayer.releaseFromJail();
                        game.setStage(GameStage.TURN_START);
                        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                                MoneyState.fromPlayer(currentPlayer))));
                        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                                currentPlayer.getName() + " is released on bail"));
                        doRegularMove(game);
                    } else {
                        int allAssets = gameHelper.computePlayerAssets(game, currentPlayer);
                        // TODO: check if property is enough: yes - event to sell some; no - auto-loose
                    }
                } else {
                    currentPlayer.doTime();
                    gameEventSender.sendToAllPlayers(SystemMessageEvent.text(currentPlayer.getName() + " is doing time"));
                    game.setStage(GameStage.TURN_START);
                    gameHelper.endTurn(game);
                }
            }
        }
    }

    @Override
    public void processBuyProposal(ProposalAction action) {
        Game game = gameHolder.getGame();
        BuyProposal buyProposal = game.getBuyProposal();
        if (!GameStage.BUY_PROPOSAL.equals(game.getStage()) || buyProposal == null) {
            throw new IllegalStateException("Cannot call buy proposal endpoint when there's no proposal");
        }
        Assert.notNull(action, NULL_ARG_MESSAGE);
        // TODO: 'security' risk: check if player id matches the proposal id
        Player buyer = buyProposal.getPlayer();
        PurchasableField field = buyProposal.getField();
        game.setBuyProposal(null);
        if (action.equals(ProposalAction.ACCEPT)) {
            gameHelper.doBuyField(game, field, field.getPrice(), buyer);
            game.setStage(GameStage.TURN_START);
            gameHelper.endTurn(game);
        } else if (action.equals(ProposalAction.DECLINE)) {
            auctionManager.startNewAuction(game, field);
        }
    }

    @Override
    public void processAuctionBuyProposal(ProposalAction action) {
        var game = gameHolder.getGame();
        auctionManager.processAuctionBuyProposal(game, action);
    }

    @Override
    public void processAuctionRaiseProposal(ProposalAction action) {
        var game = gameHolder.getGame();
        auctionManager.processAuctionRaiseProposal(game, action);
    }

    @Override
    public void processJailAction(JailAction jailAction) {
        Game game = gameHolder.getGame();
        Assert.notNull(jailAction, NULL_ARG_MESSAGE);
        Player currentPlayer = game.getCurrentPlayer();
        if (jailAction.equals(JailAction.PAY)) {
            currentPlayer.takeMoney(Rules.JAIL_BAIL);
            currentPlayer.releaseFromJail();
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                    MoneyState.fromPlayer(currentPlayer))));
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                    currentPlayer.getName() + " is released on bail"));
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
        } else if (jailAction.equals(JailAction.LUCK)) {
            gameEventSender.sendToAllPlayers(new DiceRollingStartEvent(game.getCurrentPlayer().getId()));
        }
    }

    private void doRegularMove(Game game) {
        if (!GameStage.TURN_START.equals(game.getStage())) {
            throw new IllegalStateException("cannot make move - wrong game stage");
        }
        var lastDice = game.getLastDice();
        var currentPlayer = game.getCurrentPlayer();
        checkPlayerCanMakeMove(currentPlayer);
        String currentPlayerName = currentPlayer.getName();
        var newPosition = computeNewPlayerPosition(currentPlayer, lastDice);
        gameHelper.movePlayerForward(game, currentPlayer, newPosition);
        GameField field = game.getGameMap().getField(newPosition);
        if (field instanceof PurchasableField) {
            stepProcessor.processStepOnPurchasableField(game, currentPlayer, (PurchasableField) field);
        } else if (field instanceof ActionableField) {
            var actionableField = (ActionableField) field;
            switch (actionableField.getAction()) {
                case JAIL: {
                    gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                            currentPlayerName + " is visiting Jail for a tour"));
                    gameHelper.endTurn(game);
                    break;
                }
                case CHANCE: {
                    chanceExecutor.executeChance(game);
                    break;
                }
                case ARRESTED: {
                    gameHelper.sendToJailAndEndTurn(game, currentPlayer, null);
                    break;
                }
                case PARKING: {
                    gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                            currentPlayerName + " is using free parking"));
                    gameHelper.endTurn(game);
                    break;
                }
                case INCOME_TAX: {
                    collectTax(currentPlayer, Rules.INCOME_TAX, FieldAction.INCOME_TAX.getName());
                    gameHelper.endTurn(game);
                    break;
                }
                case LUXURY_TAX: {
                    collectTax(currentPlayer, Rules.LUXURY_TAX, FieldAction.LUXURY_TAX.getName());
                    gameHelper.endTurn(game);
                    break;
                }
                default: {
                    // TODO: teleport implementation
                    // money event on start is processed on change position
                    gameHelper.endTurn(game);
                    break;
                }
            }
        } else {
            throw new IllegalStateException("field on new player position is of an unsupported type");
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

    private int computeNewPlayerPosition(Player player, DiceResult diceResult) {
        int result = player.getPosition() + diceResult.getSum();
        boolean newCircle = result > GameMap.LAST_FIELD_INDEX;
        return newCircle ? result - GameMap.NUMBER_OF_FIELDS : result;
    }

    private void collectTax(Player player, int tax, String taxName) {
        player.takeMoney(tax);
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(player))));
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s is paying $%s as %s", player.getName(), tax, taxName)));
    }
}