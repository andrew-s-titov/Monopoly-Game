package com.monopolynew.service.impl;

import com.monopolynew.dto.DiceResult;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.BuyProposalEvent;
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
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.AuctionManager;
import com.monopolynew.service.ChanceExecutor;
import com.monopolynew.service.GameHelper;
import com.monopolynew.service.GameHolder;
import com.monopolynew.service.GameService;
import com.monopolynew.websocket.GameMessageExchanger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.monopolynew.util.Message.NULL_ARG_MESSAGE;

@RequiredArgsConstructor
@Component
public class GameServiceImpl implements GameService {

    private final GameHolder gameHolder;
    private final Dice dice;
    private final GameHelper gameHelper;
    private final AuctionManager auctionManager;
    private final ChanceExecutor chanceExecutor;
    private final GameMessageExchanger gameMessageExchanger;

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
        gameMessageExchanger.sendToAllPlayers(game.mapRefreshEvent());
        Player currentPlayer = game.getCurrentPlayer();
        gameMessageExchanger.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
    }

    @Override
    public void startRolling() {
        Game game = gameHolder.getGame();
        gameMessageExchanger.sendToAllPlayers(new DiceRollingStartEvent(game.getCurrentPlayer().getId()));
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
        gameMessageExchanger.sendToAllPlayers(
                new DiceResultEvent(currentPlayer.getId(), lastDice.getFirstDice(), lastDice.getSecondDice()));
        gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(message));
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
        if (stage.equals(GameStage.TURN_START)) {
            if (currentPlayer.committedFraud()) {
                currentPlayer.resetDoublets();
                gameHelper.sendToJailAndEndTurn(game, currentPlayer, "for fraud");
            } else {
                doRegularMove(game);
            }
        } else if (stage.equals(GameStage.JAIL_RELEASE)) {
            if (lastDice.isDoublet()) {
                currentPlayer.incrementDoublets();
                currentPlayer.releaseFromJail();
                game.setStage(GameStage.TURN_START);
                gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(currentPlayer.getName() + " is pardoned under amnesty"));
                // auto move without action proposal
                doRegularMove(game);
            } else {
                if (currentPlayer.lastTurnInPrison()) {
                    if (currentPlayer.getMoney() >= Rules.JAIL_BAIL) {
                        currentPlayer.takeMoney(Rules.JAIL_BAIL);
                        gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                                MoneyState.fromPlayer(currentPlayer))));
                        gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                                currentPlayer.getName() + " is released on bail"));
                        gameMessageExchanger.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
                    } else {
                        // TODO: check if property is enough: yes - event to sell some; no - auto-loose
                    }
                } else {
                    currentPlayer.doTime();
                    gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(currentPlayer.getName() + " is doing time"));
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
            gameHelper.doBuyField(field, field.getPrice(), buyer);
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
            gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                    MoneyState.fromPlayer(currentPlayer))));
            gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                    currentPlayer.getName() + " is released on bail"));
            game.setStage(GameStage.TURN_START);
            gameMessageExchanger.sendToAllPlayers(TurnStartEvent.forPlayer(currentPlayer));
        } else if (jailAction.equals(JailAction.LUCK)) {
            gameMessageExchanger.sendToAllPlayers(new DiceRollingStartEvent(game.getCurrentPlayer().getId()));
        }
    }

    private void doRegularMove(Game game) {
        if (!GameStage.TURN_START.equals(game.getStage())) {
            throw new IllegalStateException("cannot make move - wrong game stage");
        }
        var lastDice = game.getLastDice();
        var currentPlayer = game.getCurrentPlayer();
        checkPlayerCanMakeMove(currentPlayer);
        String currentPlayerId = currentPlayer.getId();
        String currentPlayerName = currentPlayer.getName();
        var newPlayerPosition = gameHelper.movePlayer(game, currentPlayer);
        Object field = game.getGameMap().getField(newPlayerPosition);
        if (field instanceof PurchasableField) {
            PurchasableField purchasableField = (PurchasableField) field;
            String streetName = purchasableField.getName();
            int currentPlayerMoney = currentPlayer.getMoney();
            if (purchasableField.isMortgaged()) {
                gameHelper.endTurn(game);
            } else if (purchasableField.isFree()) {
                int streetPrice = purchasableField.getPrice();
                if (streetPrice <= currentPlayerMoney) {
                    var buyProposal = new BuyProposal(currentPlayer, purchasableField);
                    game.setBuyProposal(buyProposal);
                    game.setStage(GameStage.BUY_PROPOSAL);
                    gameMessageExchanger.sendToPlayer(currentPlayerId, BuyProposalEvent.fromProposal(buyProposal));
                } else {
                    // TODO: auto auction or let sell or pledge something?
                    auctionManager.startNewAuction(game, purchasableField);
                }
            } else {
                var owner = purchasableField.getOwner();
                int groupId = purchasableField.getGroup();
                Map<Integer, List<PurchasableField>> groups = game.getGameMap().getGroups();
                int fare;
                if (purchasableField instanceof StreetField) {
                    var streetField = (StreetField) purchasableField;
                    long groupOwners = groups.get(groupId).stream()
                            .map(PurchasableField::getOwner)
                            .distinct().count();
                    fare = streetField.computeRent(groupOwners == 1);
                } else if (purchasableField instanceof CompanyField) {
                    var companyField = (CompanyField) purchasableField;
                    int ownedByTheSameOwner = (int) groups.get(groupId).stream()
                            .filter(f -> f.getOwner().equals(owner))
                            .count();
                    fare = companyField.computeFare(ownedByTheSameOwner);
                } else if (purchasableField instanceof UtilityField) {
                    var utilityField = (UtilityField) purchasableField;
                    long groupOwners = groups.get(groupId).stream()
                            .map(PurchasableField::getOwner)
                            .distinct().count();
                    fare = utilityField.computeFare(lastDice, groupOwners == 1);
                } else {
                    throw new IllegalStateException("Failed to compute fair - unknown field type");
                }

                if (currentPlayerMoney >= fare) {
                    currentPlayer.takeMoney(fare);
                    owner.addMoney(fare);
                    gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                            String.format("%s is moving to the %s and paying %s $%s rent",
                                    currentPlayerName, streetName, owner.getName(), fare)));
                    gameMessageExchanger.sendToAllPlayers(
                            new MoneyChangeEvent(
                                    List.of(MoneyState.fromPlayer(currentPlayer), MoneyState.fromPlayer(owner)))
                    );
                    gameHelper.endTurn(game);
                } else {
                    int playersAssets = gameHelper.computePlayerAssets(game, currentPlayer);
                    if (playersAssets >= fare) {
                        // TODO: send message with proposal to sell something
                        // TODO: if fare > 90 % of assets - propose to give up
                    } else {
                        // auto-bankruptcy
                    }
                    gameHelper.endTurn(game); // TODO: remove on logic edit
                }
            }
        } else if (field instanceof ActionableField) {
            var actionableField = (ActionableField) field;
            switch (actionableField.getAction()) {
                case JAIL: {
                    gameMessageExchanger.sendToAllPlayers(
                            SystemMessageEvent.text(currentPlayerName + " is visiting Jail for a tour"));
                    gameHelper.endTurn(game);
                    break;
                }
                case CHANCE: {
                    chanceExecutor.executeRandomChance(game);
                    break;
                }
                case ARRESTED: {
                    gameHelper.sendToJailAndEndTurn(game, currentPlayer, null);
                    break;
                }
                case PARKING: {
                    gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                            currentPlayerName + " is using free parking"));
                    gameHelper.endTurn(game);
                    break;
                }
                case INCOME_TAX: {
                    currentPlayer.takeMoney(Rules.INCOME_TAX);
                    gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                            MoneyState.fromPlayer(currentPlayer))));
                    gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                            currentPlayerName+ " is paying income tax"));
                    gameHelper.endTurn(game);
                    break;
                }
                case LUXURY_TAX: {
                    currentPlayer.takeMoney(Rules.LUXURY_TAX);
                    gameMessageExchanger.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                            MoneyState.fromPlayer(currentPlayer))));
                    gameMessageExchanger.sendToAllPlayers(SystemMessageEvent.text(
                            currentPlayerName + " is paying luxury tax"));
                    gameHelper.endTurn(game);
                    break;
                }
                default: {
                    // TODO: teleport implementation
                    // money event on start processed on change position
                    gameHelper.endTurn(game);
                    break;
                }
            }
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
}