package com.monopolynew.service;

import com.monopolynew.dto.NewGameParamsDTO;
import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.DiceResultEvent;
import com.monopolynew.event.DiceRollingStartEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.NewPlayerTurn;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.UserInvalidInputException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Dice;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.BuyProposal;
import com.monopolynew.game.procedure.DiceResult;
import com.monopolynew.map.PurchasableField;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.monopolynew.util.CommonUtils.requireNotNullArgs;

@RequiredArgsConstructor
@Component
public class GameService {

    private final GameRepository gameRepository;
    private final Dice dice;
    private final GameLogicExecutor gameLogicExecutor;
    private final PlayerMoveService playerMoveService;
    private final AuctionManager auctionManager;
    private final GameEventSender gameEventSender;
    private final GameEventGenerator gameEventGenerator;
    private final PaymentService paymentService;
    private final FieldManagementService fieldManagementService;
    private final DealManager dealManager;
    private final GameRoomService gameRoomService;

    private final ScheduledExecutorService scheduler;

    public UUID newGame(NewGameParamsDTO newGameParamsDTO) {
        return gameRepository.createGame(newGameParamsDTO);
    }

    public boolean isGameStarted(UUID gameId) {
        return getGame(gameId).isInProgress();
    }

    public void startGame(UUID gameId) {
        Game game = getGame(gameId);
        Collection<Player> players = game.getPlayers();
        if (players.size() < 2) {
            throw new UserInvalidInputException("Cannot start a game without at least 2 players");
        }
        game.startGame();
        gameRoomService.refreshGameRooms();
        gameEventSender.sendToAllPlayers(gameId, gameEventGenerator.mapStateEvent(game));
        UUID currentPlayerId = game.getCurrentPlayer().getId();
        gameEventSender.sendToAllPlayers(gameId, new NewPlayerTurn(currentPlayerId));
        gameEventSender.sendToPlayer(gameId, currentPlayerId, new TurnStartEvent());
    }

    public void makeUsualTurn(UUID gameId) {
        Game game = getGame(gameId);
        // extract this method to a private reusable for jail action
        rollTheDice(game, this::afterDiceRollForTurn);
    }

    public void processBuyProposal(@NonNull UUID gameId, @NonNull ProposalAction action) {
        requireNotNullArgs(gameId, action);
        Game game = getGame(gameId);
        BuyProposal buyProposal = game.getBuyProposal();
        if (!GameStage.BUY_PROPOSAL.equals(game.getStage()) || buyProposal == null) {
            throw new WrongGameStageException("Cannot call buy proposal endpoint when there's no proposal");
        }
        // TODO: 'security' risk: check if player id matches the proposal id
        var buyerId = buyProposal.getPlayerId();
        PurchasableField field = buyProposal.getField();
        game.setBuyProposal(null);
        if (action.equals(ProposalAction.ACCEPT)) {
            gameLogicExecutor.doBuyField(game, field, field.getPrice(), buyerId);
            gameLogicExecutor.changeGameStage(game, GameStage.TURN_START);
            gameLogicExecutor.endTurn(game);
        } else if (action.equals(ProposalAction.DECLINE)) {
            auctionManager.startNewAuction(game, field);
        }
    }

    public void processAuctionBuyProposal(UUID gameId, ProposalAction action) {
        var game = getGame(gameId);
        auctionManager.processAuctionBuyProposal(game, action);
    }

    public void processAuctionRaiseProposal(UUID gameId, ProposalAction action) {
        var game = getGame(gameId);
        auctionManager.processAuctionRaiseProposal(game, action);
    }

    public void processJailAction(@NonNull UUID gameId, @NonNull JailAction jailAction) {
        requireNotNullArgs(gameId, jailAction);
        Game game = getGame(gameId);
        if (!GameStage.JAIL_RELEASE_START.equals(game.getStage())) {
            throw new WrongGameStageException("Cannot process jail action - wrong game stage");
        }
        Player currentPlayer = game.getCurrentPlayer();
        if (jailAction.equals(JailAction.PAY)) {
            if (currentPlayer.getMoney() < Rules.JAIL_BAIL) {
                throw new ClientBadRequestException("Not enough money to pay jail bail");
            }
            currentPlayer.takeMoney(Rules.JAIL_BAIL);
            currentPlayer.releaseFromJail();
            gameEventSender.sendToAllPlayers(gameId, new MoneyChangeEvent(Collections.singletonList(
                    MoneyState.fromPlayer(currentPlayer))));
            gameEventSender.sendToAllPlayers(gameId, new ChatMessageEvent(
                    currentPlayer.getName() + " is released on bail"));
            gameLogicExecutor.changeGameStage(game, GameStage.TURN_START);
            gameEventSender.sendToPlayer(gameId, currentPlayer.getId(), new TurnStartEvent());
        } else if (jailAction.equals(JailAction.LUCK)) {
            rollTheDice(game, this::afterDiceRollForJail);
        }
    }

    public void processPayment(UUID gameId) {
        Game game = getGame(gameId);
        GameStage currentGameStage = game.getStage();
        if (!GameStage.AWAITING_PAYMENT.equals(currentGameStage) && !GameStage.AWAITING_JAIL_FINE.equals(currentGameStage)) {
            throw new WrongGameStageException("Cannot process payment - wrong game stage");
        }
        Player debtor = paymentService.processPayment(game);
        if (GameStage.AWAITING_PAYMENT.equals(currentGameStage)) {
            gameLogicExecutor.endTurn(game);
        } else {
            debtor.releaseFromJail();
            gameLogicExecutor.changeGameStage(game, GameStage.ROLLED_FOR_TURN);
            playerMoveService.movePlayer(game, debtor, game.getLastDice());
        }
    }

    public void giveUp(UUID gameId, UUID playerId) {
        // TODO: check if game is in progress
        Game game = getGame(gameId);
        Player player = game.getPlayerById(playerId);
        gameEventSender.sendToAllPlayers(gameId, new ChatMessageEvent(player.getName() + " gave up"));
        gameLogicExecutor.bankruptPlayer(game, player);
    }

    public void mortgageField(UUID gameId, int fieldIndex, UUID playerId) {
        var game = getGame(gameId);
        fieldManagementService.mortgageField(game, fieldIndex, playerId);
    }

    public void redeemMortgagedProperty(UUID gameId, int fieldIndex, UUID playerId) {
        var game = getGame(gameId);
        fieldManagementService.redeemMortgagedProperty(game, fieldIndex, playerId);
    }

    public void buyHouse(UUID gameId, int fieldIndex, UUID playerId) {
        var game = getGame(gameId);
        fieldManagementService.buyHouse(game, fieldIndex, playerId);
    }

    public void sellHouse(UUID gameId, int fieldIndex, UUID playerId) {
        var game = getGame(gameId);
        fieldManagementService.sellHouse(game, fieldIndex, playerId);
    }

    public void createOffer(UUID gameId, UUID initiatorId, UUID addresseeId, DealOffer offer) {
        var game = getGame(gameId);
        dealManager.createOffer(game, initiatorId, addresseeId, offer);
    }

    public void processOfferAnswer(UUID gameId, UUID callerId, ProposalAction proposalAction) {
        var game = getGame(gameId);
        dealManager.processOfferAnswer(game, callerId, proposalAction);
    }


    private void rollTheDice(Game game, Consumer<Game> afterAction) {
        notifyAboutDiceRolling(game);
        var diceResult = dice.rollTheDice();
        game.setLastDice(diceResult);
        processPlayerDoublets(game);
        scheduler.schedule(
                () -> broadcastDiceResult(game, afterAction),
                1500, TimeUnit.MILLISECONDS);
    }

    private void broadcastDiceResult(Game game, Consumer<Game> afterAction) {
        var gameId = game.getId();
        var lastDice = game.getLastDice();
        Player currentPlayer = game.getCurrentPlayer();
        String message = String.format("%s rolled the dice and got %s : %s",
                currentPlayer.getName(), lastDice.getFirstDice(), lastDice.getSecondDice());
        if (lastDice.isDoublet()) {
            message = message + " (doublet)";
        }
        gameEventSender.sendToAllPlayers(gameId, DiceResultEvent.of(lastDice));
        gameEventSender.sendToAllPlayers(gameId, new ChatMessageEvent(message));

        scheduler.schedule(
                () -> afterAction.accept(game),
                2000, TimeUnit.MILLISECONDS);
    }

    private void afterDiceRollForTurn(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer.getDoubletCount() == Rules.DOUBLETS_LIMIT) {
            currentPlayer.resetDoublets();
            gameEventSender.sendToAllPlayers(game.getId(),
                    new ChatMessageEvent(currentPlayer.getName() + " was sent to jail for fraud"));
            gameLogicExecutor.sendToJail(game, currentPlayer);
            gameLogicExecutor.endTurn(game);
        } else {
            doRegularMove(game);
        }
    }

    private void afterDiceRollForJail(Game game) {
        var gameId = game.getId();
        DiceResult lastDice = game.getLastDice();
        Player currentPlayer = game.getCurrentPlayer();
        if (lastDice.isDoublet()) {
            currentPlayer.amnesty();
            gameLogicExecutor.changeGameStage(game, GameStage.ROLLED_FOR_TURN);
            gameEventSender.sendToAllPlayers(gameId, new ChatMessageEvent(
                    currentPlayer.getName() + " is pardoned under amnesty"));
            doRegularMove(game);
        } else {
            if (currentPlayer.lastTurnInPrison()) {
                String message = currentPlayer.getName() + " served time and paid a criminal fine";
                paymentService.startPaymentProcess(game, currentPlayer, null, Rules.JAIL_BAIL, message);
            } else {
                currentPlayer.doTime();
                gameEventSender.sendToAllPlayers(gameId,
                        new ChatMessageEvent(currentPlayer.getName() + " is doing time"));
                gameLogicExecutor.endTurn(game);
            }
        }
    }

    private void doRegularMove(Game game) {
        var lastDice = game.getLastDice();
        var currentPlayer = game.getCurrentPlayer();
        checkPlayerCanMakeMove(currentPlayer);
        playerMoveService.movePlayer(game, currentPlayer, lastDice);
    }

    private void checkPlayerCanMakeMove(Player player) {
        if (player.isSkipping()) {
            throw new IllegalStateException("skipping player cannot do regular turn");
        }
        if (player.isImprisoned()) {
            throw new IllegalStateException("imprisoned player cannot do regular turn");
        }
    }

    private void notifyAboutDiceRolling(Game game) {
        var newStage = GameStage.TURN_START.equals(game.getStage())
                ? GameStage.ROLLED_FOR_TURN
                : GameStage.ROLLED_FOR_JAIL;
        gameLogicExecutor.changeGameStage(game, newStage);
        gameEventSender.sendToAllPlayers(game.getId(), new DiceRollingStartEvent());
    }

    private void processPlayerDoublets(Game game) {
        var currentPlayer = game.getCurrentPlayer();
        if (game.getLastDice().isDoublet()) {
            if (GameStage.ROLLED_FOR_TURN.equals(game.getStage()) && !currentPlayer.isJustAmnestied()) {
                currentPlayer.incrementDoublets();
            }
        } else {
            currentPlayer.resetDoublets();
        }
    }

    private Game getGame(UUID gameId) {
        Game game = gameRepository.findGame(gameId);
        if (game == null) {
            throw new ClientBadRequestException("Game not found by id");
        }
        return game;
    }
}
