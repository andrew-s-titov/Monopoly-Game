package com.monopolynew.service.impl;

import com.monopolynew.dto.GameFieldView;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.MortgageChange;
import com.monopolynew.enums.GameStage;
import com.monopolynew.event.BuyProposalEvent;
import com.monopolynew.event.ChipMoveEvent;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.MortgageChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.BuyProposal;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.FieldAction;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.GameFieldConverter;
import com.monopolynew.service.GameHelper;
import com.monopolynew.websocket.GameEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class GameHelperImpl implements GameHelper {

    private final GameEventSender gameEventSender;
    private final GameFieldConverter gameFieldConverter;

    @Override
    public void movePlayerForward(Game game, Player player, int newPosition) {
        int currentPosition = player.getPosition();
        boolean newCircle = newPosition < currentPosition;
        changePlayerPosition(player, newPosition);
        if (newCircle) {
            player.addMoney(Rules.CIRCLE_MONEY);
            if (newPosition == 0) {
                player.addMoney(Rules.CIRCLE_MONEY);
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s received $%s for hitting %s field",
                                player.getName(), Rules.CIRCLE_MONEY * 2, FieldAction.START.getName())));
            } else {
                gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                        String.format("%s received $%s for starting a new circle",
                                player.getName(), Rules.CIRCLE_MONEY)));
            }
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    Collections.singletonList(MoneyState.fromPlayer(player))));
        }
    }

    @Override
    public void changePlayerPosition(Player player, int fieldId) {
        player.changePosition(fieldId);
        gameEventSender.sendToAllPlayers(new ChipMoveEvent(player.getId(), fieldId));
    }

    @Override
    public void sendToJailAndEndTurn(Game game, Player player, @Nullable String reason) {
        player.resetDoublets();
        player.imprison();
        gameEventSender.sendToAllPlayers(
                SystemMessageEvent.text(player.getName() + " was sent to jail " + (reason == null ? "" : reason)));
        changePlayerPosition(player, GameMap.JAIL_FIELD_NUMBER);
        endTurn(game);
    }

    @Override
    public void sendBuyProposal(Game game, Player player, PurchasableField field) {
        var buyProposal = new BuyProposal(player, field);
        game.setBuyProposal(buyProposal);
        game.setStage(GameStage.BUY_PROPOSAL);
        gameEventSender.sendToPlayer(player.getId(), BuyProposalEvent.fromProposal(buyProposal));
    }

    @Override
    public void doBuyField(Game game, PurchasableField field, int price, Player player) {
        field.newOwner(player);
        player.takeMoney(price);
        gameEventSender.sendToAllPlayers(SystemMessageEvent.text(
                String.format("%s is buying %s for $%s", player.getName(), field.getName(), price)));
        gameEventSender.sendToAllPlayers(new MoneyChangeEvent(Collections.singletonList(
                MoneyState.fromPlayer(player))));

        int fieldGroupId = field.getGroupId();
        List<GameFieldView> newPriceViews;
        List<PurchasableField> fieldGroup = game.getGameMap().getGroup(fieldGroupId);
        if (field instanceof StreetField) {
            boolean allGroupOwnedByTheSameOwner = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            if (allGroupOwnedByTheSameOwner) {
                fieldGroup.stream()
                        .map(streetField -> (StreetField) streetField)
                        .forEach(streetField -> streetField.setNewRent(true));
                newPriceViews = fieldGroup.stream()
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                ((StreetField) field).setNewRent(false);
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else if (field instanceof CompanyField) {
            int ownedByTheSamePlayer = (int) fieldGroup.stream()
                    .filter(f -> !f.isFree())
                    .filter(f -> f.getOwner().equals(field.getOwner()))
                    .count();
            if (ownedByTheSamePlayer > 1) {
                fieldGroup.stream()
                        .map(companyField -> (CompanyField) companyField)
                        .forEach(companyField -> companyField.setNewRent(ownedByTheSamePlayer));
                newPriceViews = fieldGroup.stream()
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                ((CompanyField) field).setNewRent(ownedByTheSamePlayer);
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else if (field instanceof UtilityField) {
            boolean increasedMultiplier = fieldGroup.stream()
                    .noneMatch(PurchasableField::isFree)
                    && fieldGroup.stream()
                    .allMatch(f -> player.equals(f.getOwner()));
            if (increasedMultiplier) {
                fieldGroup.stream()
                        .map(utilityField -> (UtilityField) utilityField)
                        .forEach(UtilityField::increaseMultiplier);
                newPriceViews = fieldGroup.stream()
                        .map(utilityField -> (UtilityField) utilityField)
                        .map(gameFieldConverter::toView)
                        .collect(Collectors.toList());
            } else {
                newPriceViews = Collections.singletonList(gameFieldConverter.toView(field));
            }
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(newPriceViews));
    }

    @Override
    public int computePlayerAssets(Game game, Player player) {
        int assetSum = player.getMoney();
        List<PurchasableField> countedFields = game.getGameMap().getFields().stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(field -> player.equals(field.getOwner()))
                .filter(field -> !field.isMortgaged())
                .collect(Collectors.toList());
        for (PurchasableField field : countedFields) {
            if (field instanceof StreetField) {
                int houses = ((StreetField) field).getHouses();
                int housePrice = ((StreetField) field).getHousePrice();
                assetSum += housePrice * houses;
            }
            int price = field.getPrice();
            assetSum += price / 2;
        }
        return assetSum;
    }

    @Override
    public void endTurn(Game game) {
        if (!GameStage.ROLLED_FOR_JAIL.equals(game.getStage())) {
            // as there was no actual turn when turn ended after rolled for jail (tried luck but hot nota a doublet)
            processMortgage(game);
        }
        game.getGameMap().resetPurchaseHistory();

        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer.isAmnestied()) {
            currentPlayer.clearCriminalRecord();
        }
        Player nextPlayer;
        if (game.getLastDice().isDoublet() && !currentPlayer.isSkipping()
                && !currentPlayer.isImprisoned() && !currentPlayer.isAmnestied()) {
            nextPlayer = currentPlayer;
        } else {
            nextPlayer = toNextPlayer(game);
        }
        if (nextPlayer.isImprisoned()) {
            game.setStage(GameStage.JAIL_RELEASE_START);
            String playerId = nextPlayer.getId();
            gameEventSender.sendToAllPlayers(new JailReleaseProcessEvent(
                    playerId, nextPlayer.getMoney() >= Rules.JAIL_BAIL));
        } else {
            game.setStage(GameStage.TURN_START);
            gameEventSender.sendToAllPlayers(TurnStartEvent.forPlayer(nextPlayer));
        }
    }

    private Player toNextPlayer(Game game) {
        Player nextPlayer = game.nextPlayer();
        if (nextPlayer.isSkipping()) {
            gameEventSender.sendToAllPlayers(SystemMessageEvent.text(nextPlayer.getName() + " is skipping his/her turn"));
            nextPlayer.skip();
            nextPlayer = toNextPlayer(game);
        }
        return nextPlayer;
    }

    private void processMortgage(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        List<MortgageChange> mortgageChanges = new ArrayList<>(30);
        List<GameFieldView> fieldViews = new ArrayList<>(30);
        game.getGameMap().getFields().stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(PurchasableField::isMortgaged)
                .forEach(field -> {
                    if (!field.isMortgagedDuringThisTurn() && field.getOwner().equals(currentPlayer)) {
                        int mortgageTurns = field.decreaseMortgageTurns();
                        mortgageChanges.add(new MortgageChange(field.getId(), field.getMortgageTurnsLeft()));
                        if (mortgageTurns == 0) {
                            field.newOwner(null);
                            fieldViews.add(gameFieldConverter.toView(field));
                        }
                    }
                });
        if (!CollectionUtils.isEmpty(mortgageChanges)) {
            gameEventSender.sendToAllPlayers(new MortgageChangeEvent(mortgageChanges));
        }
        if (!CollectionUtils.isEmpty(fieldViews)) {
            gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(fieldViews));
        }
    }
}
