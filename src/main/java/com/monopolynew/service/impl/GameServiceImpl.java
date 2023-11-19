package com.monopolynew.service.impl;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.PreDealInfo;
import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.PlayerManagementAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.DiceRollingStartEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.PlayerInvalidInputException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Dice;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.GameField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.DealManager;
import com.monopolynew.service.FieldManagementService;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameLogicExecutor;
import com.monopolynew.service.GameRepository;
import com.monopolynew.service.GameService;
import com.monopolynew.service.PaymentProcessor;
import com.monopolynew.service.StepProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final Dice dice;
    private final GameLogicExecutor gameLogicExecutor;
    private final StepProcessor stepProcessor;
    private final AuctionManager auctionManager;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;
    private final PaymentProcessor paymentProcessor;
    private final FieldManagementService fieldManagementService;
    private final DealManager dealManager;

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
        var player = gameRepository.getGame().getPlayerById(playerId);
        return player == null ? null :  player.getName();
    }

    @Override
    public void startGame() {
        Game game = gameRepository.getGame();
        Collection<Player> players = game.getPlayers();
        if (players.size() < 2) {
            throw new PlayerInvalidInputException("Cannot start a game without at least 2 players");
        }
        game.startGame();
        gameEventSender.sendToAllPlayers(gameEventGenerator.newMapRefreshEvent(game));
        gameEventSender.sendToAllPlayers(new TurnStartEvent(game.getCurrentPlayer().getId()));
    }

    @Override
    public void startDiceRolling() {
        Game game = gameRepository.getGame();
        notifyAboutDiceRolling(game);
        var lastDice = dice.rollTheDice();
        game.setLastDice(lastDice);
        var currentGameStage = game.getStage();
        var currentPlayer = game.getCurrentPlayer();
        if (lastDice.isDoublet()) {
            if (GameStage.ROLLED_FOR_TURN.equals(currentGameStage) && !currentPlayer.isAmnestied()) {
                currentPlayer.incrementDoublets();
            }
        } else {
            currentPlayer.resetDoublets();
        }
    }

    @Override
    public void broadcastDiceResult() {
        Game game = gameRepository.getGame();
        GameStage stage = game.getStage();
        if (GameStage.ROLLED_FOR_TURN.equals(stage) || GameStage.ROLLED_FOR_JAIL.equals(stage)) {
            var lastDice = game.getLastDice();
            Player currentPlayer = game.getCurrentPlayer();
            String message = String.format("%s rolled the dice and got %s : %s",
                    currentPlayer.getName(), lastDice.getFirstDice(), lastDice.getSecondDice());
            if (lastDice.isDoublet()) {
                message = message + " (doublet)";
            }
            gameEventSender.sendToAllPlayers(
                    new DiceResultEvent(currentPlayer.getId(), lastDice.getFirstDice(), lastDice.getSecondDice()));
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(message));
        }
    }

    @Override
    public void afterDiceRollAction() {
        Game game = gameRepository.getGame();
        DiceResult lastDice = game.getLastDice();
        if (lastDice == null) {
            throw new ClientBadRequestException("throw dice must be called first");
        }
        Player currentPlayer = game.getCurrentPlayer();
        GameStage stage = game.getStage();
        if (GameStage.ROLLED_FOR_TURN.equals(stage)) {
            if (currentPlayer.committedFraud()) {
                currentPlayer.resetDoublets();
                gameLogicExecutor.sendToJailAndEndTurn(game, currentPlayer, "for fraud");
            } else {
                doRegularMove(game);
            }
        } else if (GameStage.ROLLED_FOR_JAIL.equals(stage)) {
            if (lastDice.isDoublet()) {
                currentPlayer.amnesty();
                game.setStage(GameStage.ROLLED_FOR_TURN);
                gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                        currentPlayer.getName() + " is pardoned under amnesty"));
                doRegularMove(game);
            } else {
                if (currentPlayer.lastTurnInPrison()) {
                    String message = currentPlayer.getName() + " served time and paid a criminal fine";
                    paymentProcessor.startPaymentProcess(game, currentPlayer, null, Rules.JAIL_BAIL, message);
                } else {
                    currentPlayer.doTime();
                    gameEventSender.sendToAllPlayers(new ChatMessageEvent(currentPlayer.getName() + " is doing time"));
                    gameLogicExecutor.endTurn(game);
                }
            }
        } else {
            throw new WrongGameStageException("Cannot process dice - wrong game stage (roll must be called first)");
        }
    }

    @Override
    public void afterPlayerMoveAction() {
        Game game = gameRepository.getGame();
        if (!GameStage.ROLLED_FOR_TURN.equals(game.getStage())) {
            throw new WrongGameStageException("Cannot define after player move action - wrong game stage");
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
            throw new WrongGameStageException("Cannot call buy proposal endpoint when there's no proposal");
        }
        Assert.notNull(action, NULL_ARG_MESSAGE);
        // TODO: 'security' risk: check if player id matches the proposal id
        var buyerId = buyProposal.getPlayerId();
        PurchasableField field = buyProposal.getField();
        game.setBuyProposal(null);
        if (action.equals(ProposalAction.ACCEPT)) {
            gameLogicExecutor.doBuyField(game, field, field.getPrice(), buyerId);
            game.setStage(GameStage.TURN_START);
            gameLogicExecutor.endTurn(game);
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
            throw new WrongGameStageException("Cannot process jail action - wrong game stage");
        }
        Assert.notNull(jailAction, NULL_ARG_MESSAGE);
        Player currentPlayer = game.getCurrentPlayer();
        if (jailAction.equals(JailAction.PAY)) {
            if (currentPlayer.getMoney() < Rules.JAIL_BAIL) {
                throw new ClientBadRequestException("Not enough money to pay jail bail");
            }
            currentPlayer.takeMoney(Rules.JAIL_BAIL);
            currentPlayer.releaseFromJail();
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                    MoneyState.fromPlayer(currentPlayer))));
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                    currentPlayer.getName() + " is released on bail"));
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(new TurnStartEvent(currentPlayer.getId()));
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
    public void giveUp(String playerId) {
        // TODO: check if game is in progress
        Game game = gameRepository.getGame();
        Player player = game.getPlayerById(playerId);
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(player.getName() + " gave up"));
        gameLogicExecutor.bankruptPlayer(game, player, null);
    }

    @Override
    public List<FieldManagementAction> availableFieldManagementActions(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        return fieldManagementService.availableManagementActions(game, fieldIndex, playerId);
    }

    @Override
    public List<PlayerManagementAction> availablePlayerManagementActions(String requestingPlayerId, String subjectPlayerId) {
        var game = gameRepository.getGame();
        List<PlayerManagementAction> actions = new ArrayList<>(3);

        if (requestingPlayerId.equals(subjectPlayerId)) {
            actions.add(PlayerManagementAction.GIVE_UP);
        } else {
            var currentPlayer = game.getCurrentPlayer();
            var gameStage = game.getStage();
            if (currentPlayer.getId().equals(requestingPlayerId) &&
                    (GameStage.TURN_START.equals(gameStage) || GameStage.JAIL_RELEASE_START.equals(gameStage))) {
                actions.add(PlayerManagementAction.OFFER);
            }
        }
        return actions;
    }

    @Override
    public void mortgageField(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        var moneyHistory = fieldManagementService.mortgageField(game, fieldIndex, playerId);
        afterManagementStateCheck(game, moneyHistory);
    }

    @Override
    public void redeemMortgagedProperty(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        var moneyHistory = fieldManagementService.redeemMortgagedProperty(game, fieldIndex, playerId);
        afterManagementStateCheck(game, moneyHistory);
    }

    @Override
    public void buyHouse(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        var moneyHistory = fieldManagementService.buyHouse(game, fieldIndex, playerId);
        afterManagementStateCheck(game, moneyHistory);
    }

    @Override
    public void sellHouse(int fieldIndex, String playerId) {
        var game = gameRepository.getGame();
        var moneyHistory = fieldManagementService.sellHouse(game, fieldIndex, playerId);
        afterManagementStateCheck(game, moneyHistory);
    }

    @Override
    public PreDealInfo getPreDealInfo(String offerInitiatorId, String offerAddresseeId) {
        var game = gameRepository.getGame();
        return dealManager.getPreDealInfo(game, offerInitiatorId, offerAddresseeId);
    }

    @Override
    public void createOffer(String offerInitiatorId, String offerAddresseeId, DealOffer offer) {
        var game = gameRepository.getGame();
        dealManager.createOffer(game, offerInitiatorId, offerAddresseeId, offer);
    }

    @Override
    public void processOfferAnswer(String callerId, ProposalAction proposalAction) {
        var game = gameRepository.getGame();
        dealManager.processOfferAnswer(game, callerId, proposalAction);
    }

    private void doRegularMove(Game game) {
        if (!GameStage.ROLLED_FOR_TURN.equals(game.getStage())) {
            throw new WrongGameStageException("cannot make move - wrong game stage");
        }
        var lastDice = game.getLastDice();
        var currentPlayer = game.getCurrentPlayer();
        checkPlayerCanMakeMove(currentPlayer);
        var newPosition = gameLogicExecutor.computeNewPlayerPosition(currentPlayer, lastDice);
        gameLogicExecutor.movePlayer(game, currentPlayer, newPosition, true);
    }

    private void checkPlayerCanMakeMove(Player player) {
        if (player.isSkipping()) {
            throw new ClientBadRequestException("skipping player cannot do regular turn");
        }
        if (player.isImprisoned()) {
            throw new ClientBadRequestException("imprisoned player cannot do regular turn");
        }
    }

    private void afterManagementStateCheck(Game game, Pair<Integer, Integer> managementMoneyInfo) {
        int beforeManagementMoneyAmount = managementMoneyInfo.getLeft();
        int afterManagementMoneyAmount = managementMoneyInfo.getRight();
        var playerId = game.getCurrentPlayer().getId();

        GameStage stage = game.getStage();
        if ((GameStage.AWAITING_PAYMENT.equals(stage) || GameStage.AWAITING_JAIL_FINE.equals(stage))) {
            var checkToPay = game.getCheckToPay();
            var sumToPay = checkToPay.getSum();
            var checkIsPayable = checkToPay.isPayable();
            if (checkIsPayable && afterManagementMoneyAmount < sumToPay) {
                checkToPay.setPayable(false);
                gameEventSender.sendToPlayer(playerId, gameEventGenerator.newPayCommandEvent(checkToPay));
            } else if (!checkIsPayable && afterManagementMoneyAmount >= sumToPay) {
                checkToPay.setPayable(true);
                gameEventSender.sendToPlayer(playerId, gameEventGenerator.newPayCommandEvent(checkToPay));
            }
        } else if (GameStage.JAIL_RELEASE_START.equals(stage)) {
            if (beforeManagementMoneyAmount < Rules.JAIL_BAIL && afterManagementMoneyAmount >= Rules.JAIL_BAIL) {
                gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(playerId, true));
            } else if (beforeManagementMoneyAmount >= Rules.JAIL_BAIL && afterManagementMoneyAmount < Rules.JAIL_BAIL) {
                gameEventSender.sendToPlayer(playerId, new JailReleaseProcessEvent(playerId, false));
            }
        } else if (GameStage.BUY_PROPOSAL.equals(stage)) {
            var buyProposal = game.getBuyProposal();
            var fieldPrice = buyProposal.getField().getPrice();
            var proposalIsPayable = buyProposal.isPayable();
            if (proposalIsPayable && afterManagementMoneyAmount < fieldPrice) {
                buyProposal.setPayable(false);
                gameEventSender.sendToPlayer(playerId, gameEventGenerator.newBuyProposalEvent(buyProposal));
            } else if (!proposalIsPayable && afterManagementMoneyAmount >= fieldPrice) {
                buyProposal.setPayable(true);
                gameEventSender.sendToPlayer(playerId, gameEventGenerator.newBuyProposalEvent(buyProposal));
            }
        }
    }

    private void notifyAboutDiceRolling(Game game) {
        GameStage currentStage = game.getStage();
        if (GameStage.TURN_START.equals(currentStage) || GameStage.JAIL_RELEASE_START.equals(currentStage)) {
            game.setStage(GameStage.TURN_START.equals(currentStage) ? GameStage.ROLLED_FOR_TURN : GameStage.ROLLED_FOR_JAIL);
        } else {
            throw new WrongGameStageException("cannot roll the dice - wrong game stage");
        }
        gameEventSender.sendToAllPlayers(new DiceRollingStartEvent(game.getCurrentPlayer().getId()));
    }
}