package com.monopolynew.service.impl;

import com.monopolynew.dto.CheckToPay;
import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.DiceRollingStartEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.PayCommandEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Dice;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.FieldManagementService;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.GameMapRefresher;
import com.monopolynew.service.GameRepository;
import com.monopolynew.service.GameService;
import com.monopolynew.service.PaymentProcessor;
import com.monopolynew.service.StepProcessor;
import com.monopolynew.util.TriConsumer;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final Dice dice;
    private final GameHelper gameHelper;
    private final StepProcessor stepProcessor;
    private final AuctionManager auctionManager;
    private final GameEventSender gameEventSender;
    private final GameMapRefresher gameMapRefresher;
    private final PaymentProcessor paymentProcessor;
    private final FieldManagementService fieldManagementService;

    @Override
    public boolean isGameStarted() {
        return gameRepository.getGame().isInProgress();
    }

    @Override
    public boolean usernameTaken(String username) {
        return gameRepository.getGame().isUsernameTaken(username);
    }

    @Override
    public String getPlayerName(String playerId) {
        return gameRepository.getGame().getPlayerById(playerId).getName();
    }

    @Override
    public void startGame() {
        Game game = gameRepository.getGame();
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
        Game game = gameRepository.getGame();
        notifyAboutDiceRolling(game);
    }

    @Override
    public void doRollTheDice() {
        Game game = gameRepository.getGame();
        GameStage stage = game.getStage();
        if (GameStage.ROLLED_FOR_TURN.equals(stage) || GameStage.ROLLED_FOR_JAIL.equals(stage)) {
            var lastDice = dice.rollTheDice();
            game.setLastDice(lastDice);
            Player currentPlayer = game.getCurrentPlayer();
            String message = String.format("%s rolled the dice and got %s : %s",
                    currentPlayer.getName(), lastDice.getFirstDice(), lastDice.getSecondDice());
            if (lastDice.isDoublet()) {
                message = message + " (doublet)";
                if (stage.equals(GameStage.ROLLED_FOR_TURN) && !currentPlayer.isAmnestied()) {
                    currentPlayer.incrementDoublets();
                }
            } else {
                currentPlayer.resetDoublets();
            }
            gameEventSender.sendToAllPlayers(
                    new DiceResultEvent(currentPlayer.getId(), lastDice.getFirstDice(), lastDice.getSecondDice()));
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(message));
        }
    }

    @Override
    public void afterDiceRollAction() {
        Game game = gameRepository.getGame();
        DiceResult lastDice = game.getLastDice();
        if (lastDice == null) {
            throw new IllegalStateException("throw dice must be called first");
        }
        Player currentPlayer = game.getCurrentPlayer();
        GameStage stage = game.getStage();
        if (GameStage.ROLLED_FOR_TURN.equals(stage)) {
            if (currentPlayer.committedFraud()) {
                currentPlayer.resetDoublets();
                gameHelper.sendToJailAndEndTurn(game, currentPlayer, "for fraud");
            } else {
                doRegularMove(game);
            }
        } else if (GameStage.ROLLED_FOR_JAIL.equals(stage)) {
            if (lastDice.isDoublet()) {
                currentPlayer.amnesty();
                game.setStage(GameStage.ROLLED_FOR_TURN);
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        currentPlayer.getName() + " is pardoned under amnesty"));
                doRegularMove(game);
            } else {
                if (currentPlayer.lastTurnInPrison()) {
                    if (currentPlayer.getMoney() >= Rules.JAIL_BAIL) {
                        currentPlayer.takeMoney(Rules.JAIL_BAIL);
                        currentPlayer.releaseFromJail();
                        game.setStage(GameStage.ROLLED_FOR_TURN);
                        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                                MoneyState.fromPlayer(currentPlayer))));
                        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                                currentPlayer.getName() + " is released on bail"));
                        doRegularMove(game);
                    } else {
                        int availableAssets = gameHelper.computePlayerAssets(game, currentPlayer);
                        if (availableAssets >= Rules.JAIL_BAIL) {
                            String message = currentPlayer.getName() + " served time and paid a criminal fine";
                            paymentProcessor.createPayCheck(game, currentPlayer, null, Rules.JAIL_BAIL, message);
                        } else {
                            // TODO: auto-bankruptcy
                            gameHelper.endTurn(game);
                        }
                    }
                } else {
                    currentPlayer.doTime();
                    gameEventSender.sendToAllPlayers(SystemMessageEvent.text(currentPlayer.getName() + " is doing time"));
                    gameHelper.endTurn(game);
                }
            }
        } else {
            throw new IllegalStateException("Cannot process dice - wrong game stage (roll must be called first)");
        }
    }

    @Override
    public void afterPlayerMoveAction() {
        Game game = gameRepository.getGame();
        if (!GameStage.ROLLED_FOR_TURN.equals(game.getStage())) {
            throw new IllegalStateException("Cannot define after player move action - wrong game stage");
        }
        int playerPosition = game.getCurrentPlayer().getPosition();
        GameField currentField = game.getGameMap().getField(playerPosition);
        stepProcessor.processStepOnField(game, currentField);
    }

    @Override
    public void processBuyProposal(ProposalAction action) {
        Game game = gameRepository.getGame();
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
        var game = gameRepository.getGame();
        auctionManager.processAuctionBuyProposal(game, action);
    }

    @Override
    public void processAuctionRaiseProposal(ProposalAction action) {
        var game = gameRepository.getGame();
        auctionManager.processAuctionRaiseProposal(game, action);
    }

    @Override
    public void processJailAction(JailAction jailAction) {
        Game game = gameRepository.getGame();
        if (!GameStage.JAIL_RELEASE_START.equals(game.getStage())) {
            throw new IllegalStateException("Cannot process jail action - wrong game stage");
        }
        Assert.notNull(jailAction, NULL_ARG_MESSAGE);
        Player currentPlayer = game.getCurrentPlayer();
        if (jailAction.equals(JailAction.PAY)) {
            if (currentPlayer.getMoney() < Rules.JAIL_BAIL) {
                throw new IllegalStateException("Not enough money to pay jail bail");
            }
            currentPlayer.takeMoney(Rules.JAIL_BAIL);
            currentPlayer.releaseFromJail();
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                    MoneyState.fromPlayer(currentPlayer))));
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                    currentPlayer.getName() + " is released on bail"));
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
        } else if (jailAction.equals(JailAction.LUCK)) {
            notifyAboutDiceRolling(game);
        }
    }

    @Override
    public void processPayment() {
        Game game = gameRepository.getGame();
        paymentProcessor.processPayment(game);
    }

    @Override
    public void giveUp() {
        // TODO: implementation
    }

    @Override
    public List<FieldManagementAction> availableManagementActions(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        return fieldManagementService.availableManagementActions(game, fieldIndex, playerId);
    }

    @Override
    public void mortgageField(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        managementWithPayCheckResend(game, fieldIndex, playerId, fieldManagementService::mortgageField);
    }

    @Override
    public void redeemMortgagedProperty(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        managementWithPayCheckResend(game, fieldIndex, playerId, fieldManagementService::redeemMortgagedProperty);
    }

    @Override
    public void buyHouse(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        managementWithPayCheckResend(game, fieldIndex, playerId, fieldManagementService::buyHouse);
    }

    @Override
    public void sellHouse(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        managementWithPayCheckResend(game, fieldIndex, playerId, fieldManagementService::sellHouse);
    }

    private void doRegularMove(Game game) {
        if (!GameStage.ROLLED_FOR_TURN.equals(game.getStage())) {
            throw new IllegalStateException("cannot make move - wrong game stage");
        }
        var lastDice = game.getLastDice();
        var currentPlayer = game.getCurrentPlayer();
        checkPlayerCanMakeMove(currentPlayer);
        var newPosition = computeNewPlayerPosition(currentPlayer, lastDice);
        gameHelper.movePlayer(game, currentPlayer, newPosition, true);
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

    private void managementWithPayCheckResend(Game game, int fieldIndex, String playerId, TriConsumer<Game, Integer, String> managementAction) {
        int playerMoneyBeforeManagement = game.getCurrentPlayer().getMoney();
        managementAction.apply(game, fieldIndex, playerId);
        int playerMoneyAfterManagement = game.getCurrentPlayer().getMoney();

        GameStage stage = game.getStage();
        if ((GameStage.AWAITING_PAYMENT.equals(stage) || GameStage.AWAITING_JAIL_FINE.equals(stage))) {
            CheckToPay checkToPay = game.getCheckToPay();
            int sum = checkToPay.getSum();
            if (checkToPay.isPayable() && playerMoneyAfterManagement < sum) {
                checkToPay.setPayable(false);
                gameEventSender.sendToPlayer(playerId, PayCommandEvent.fromCheck(checkToPay));
            } else if (!checkToPay.isPayable() && playerMoneyAfterManagement >= sum) {
                checkToPay.setPayable(true);
                gameEventSender.sendToPlayer(playerId, PayCommandEvent.fromCheck(checkToPay));
            }
        } else if (GameStage.JAIL_RELEASE_START.equals(stage)) {
            if (playerMoneyBeforeManagement < Rules.JAIL_BAIL && playerMoneyAfterManagement >= Rules.JAIL_BAIL) {
                gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(playerId, true));
            } else if (playerMoneyBeforeManagement >= Rules.JAIL_BAIL && playerMoneyAfterManagement < Rules.JAIL_BAIL) {
                gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(playerId, false));
            }
        }
    }

    private void notifyAboutDiceRolling(Game game) {
        GameStage currentStage = game.getStage();
        if (GameStage.TURN_START.equals(currentStage) || GameStage.JAIL_RELEASE_START.equals(currentStage)) {
            game.setStage(GameStage.TURN_START.equals(currentStage) ? GameStage.ROLLED_FOR_TURN : GameStage.ROLLED_FOR_JAIL);
        } else {
            throw new IllegalStateException("cannot roll the dice - wrong game stage");
        }
        gameEventSender.sendToAllPlayers(DiceRollingStartEvent.forPlayer(game.getCurrentPlayer()));
    }
}