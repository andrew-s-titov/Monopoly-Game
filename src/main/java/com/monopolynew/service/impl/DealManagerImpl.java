package com.monopolynew.service.impl;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.dto.PreDealInfo;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.OfferProcessedEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.Offer;
import com.monopolynew.map.GameField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.service.DealManager;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameFieldConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DealManagerImpl implements DealManager {

    // TODO: exception handling, status codes to identify error

    private final GameEventGenerator gameEventGenerator;
    private final GameFieldConverter gameFieldConverter;
    private final GameEventSender gameEventSender;

    @Override
    public PreDealInfo getPreDealInfo(Game game, String offerInitiatorId, String offerAddresseeId) {
        checkCanCreateOffer(game);
        checkDealSides(game, offerInitiatorId, offerAddresseeId);
        List<GameField> fields = game.getGameMap().getFields();
        var offerInitiatorFieldList = getPlayerFields(fields, offerInitiatorId);
        var offerAddresseeFieldList = getPlayerFields(fields, offerAddresseeId);
        return new PreDealInfo(
                gameFieldConverter.toListOfferView(offerInitiatorFieldList),
                gameFieldConverter.toListOfferView(offerAddresseeFieldList));
    }

    @Override
    public void createOffer(Game game, String offerInitiatorId, String offerAddresseeId, DealOffer offer) {
        checkCanCreateOffer(game);
        checkDealSides(game, offerInitiatorId, offerAddresseeId);
        var currentPlayer = game.getCurrentPlayer();
        var offerAddressee = getOfferAddressee(game, offerAddresseeId);
        checkOfferNotEmpty(offer);
        var moneyToGive = offer.getMoneyToGive();
        var moneyToReceive = offer.getMoneyToReceive();
        checkPlayerSolvency(currentPlayer, moneyToGive);
        checkPlayerSolvency(offerAddressee, moneyToReceive);
        var fieldsToBuy = getFieldsByIndexes(game, offer.getFieldsToBuy());
        var fieldsToSell = getFieldsByIndexes(game, offer.getFieldsToSell());
        checkFieldsOwner(fieldsToBuy, offerAddressee);
        checkFieldsOwner(fieldsToSell, currentPlayer);
        var currentGameStage = game.getStage();
        game.setStage(GameStage.DEAL_OFFER);

        var newOffer = Offer.builder()
                .initiator(currentPlayer)
                .addressee(offerAddressee)
                .fieldsToSell(fieldsToSell)
                .fieldsToBuy(fieldsToBuy)
                .moneyToGive(moneyToGive)
                .moneyToReceive(moneyToReceive)
                .stageToReturnTo(currentGameStage)
                .build();
        game.setOffer(newOffer);
        gameEventSender.sendToAllPlayers(new SystemMessageEvent(
                String.format("%s offered %s a deal", currentPlayer.getName(), offerAddressee.getName())));
        gameEventSender.sendToPlayer(offerAddresseeId, gameEventGenerator.newOfferProposalEvent(game));
    }

    @Override
    public void processOfferAnswer(Game game, String callerId, ProposalAction proposalAction) {
        var currentGameStage = game.getStage();
        if (!GameStage.DEAL_OFFER.equals(currentGameStage)) {
            throw new IllegalStateException("cannot process offer - wrong game stage");
        }
        var offer = game.getOffer();
        var offerAddressee = offer.getAddressee();
        if (!offerAddressee.getId().equals(callerId)) {
            // for security reasons
            throw new IllegalStateException("only offer addressee can process offer");
        }
        var offerInitiator = offer.getInitiator();
        if (ProposalAction.DECLINE.equals(proposalAction)) {
            gameEventSender.sendToAllPlayers(new SystemMessageEvent(offerAddressee.getName() + " declined the offer"));
        } else {
            gameEventSender.sendToAllPlayers(new SystemMessageEvent(offerAddressee.getName() + " accepted the offer"));
            processOfferPayment(offer.getMoneyToGive(), offerInitiator, offerAddressee);
            processOfferPayment(offer.getMoneyToReceive(), offerAddressee, offerInitiator);
            processOfferFieldExchange(offer.getFieldsToBuy(), offerInitiator);
            processOfferFieldExchange(offer.getFieldsToSell(), offerAddressee);
        }
        game.setOffer(null);
        GameStage stageToReturnTo = offer.getStageToReturnTo();
        game.setStage(stageToReturnTo);
        String offerInitiatorId = offerInitiator.getId();
        gameEventSender.sendToPlayer(offerInitiatorId, new OfferProcessedEvent());
        if (GameStage.TURN_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(offerInitiatorId, new TurnStartEvent(offerInitiatorId));
        }
        if (GameStage.JAIL_RELEASE_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(offerInitiatorId,
                    new JailReleaseProcessEvent(offerInitiatorId, offerInitiator.getMoney() >= Rules.JAIL_BAIL));
        }
    }

    private List<PurchasableField> getPlayerFields(List<GameField> fields, String playerId) {
        return fields.stream()
                .filter(field -> field instanceof PurchasableField)
                .map(field -> (PurchasableField) field)
                .filter(field -> !field.isFree())
                .filter(field -> field.getOwner().getId().equals(playerId))
                .collect(Collectors.toList());
    }

    private void checkDealSides(Game game, String offerInitiatorId, String offerAddresseeId) {
        checkOfferInitiator(game, offerInitiatorId);
        getOfferAddressee(game, offerAddresseeId);
    }

    private void checkCanCreateOffer(Game game) {
        var currentGameStage = game.getStage();
        if (!currentGameStage.equals(GameStage.TURN_START) && !currentGameStage.equals(GameStage.JAIL_RELEASE_START)) {
            throw new IllegalStateException("cannot start deal process - wrong game stage");
        }
    }

    private void checkOfferInitiator(Game game, String offerInitiatorId) {
        if (!game.getCurrentPlayer().getId().equals(offerInitiatorId)) {
            throw new IllegalStateException("only current player can initiate deal process");
        }
    }

    private Player getOfferAddressee(Game game, String offerAddresseeId) {
        var offerAddressee = game.getPlayerById(offerAddresseeId);
        if (offerAddressee == null) {
            throw new IllegalArgumentException(String.format("player with id %s doesn't exist", offerAddresseeId));
        }
        return offerAddressee;
    }

    private List<PurchasableField> getFieldsByIndexes(Game game, List<Integer> indexes) {
        var gameMap = game.getGameMap();
        if (CollectionUtils.isEmpty(indexes)) {
            return Collections.emptyList();
        } else {
            return indexes.stream()
                    .map(index -> safeGetFieldByIndex(gameMap, index))
                    .collect(Collectors.toList());
        }
    }

    private PurchasableField safeGetFieldByIndex(GameMap gameMap, Integer index) {
        if (index < 0 || index > GameMap.LAST_FIELD_INDEX) {
            throw new IllegalArgumentException("Field index must be from 0 to " + GameMap.LAST_FIELD_INDEX);
        }
        var gameField = gameMap.getField(index);
        if (!(gameField instanceof PurchasableField)) {
            throw new IllegalArgumentException("Field with index " + index + " cannot be a subject for a deal");
        }
        return (PurchasableField) gameField;
    }

    private void checkPlayerSolvency(Player player, Integer money) {
        if (money != null && player.getMoney() < money) {
            throw new IllegalArgumentException(String.format("player %s cannot afford this deal", player.getName()));
        }
    }

    private void checkFieldsOwner(List<PurchasableField> fields, Player owner) {
        for (PurchasableField field : fields) {
            if (!owner.equals(field.getOwner())) {
                throw new IllegalArgumentException(
                        String.format("field %s doesn't belong to the player %s", field.getId(), owner.getId()));
            }
        }
    }

    private void processOfferPayment(Integer moneyAmount, Player payer, Player recipient) {
        if (moneyAmount != null && moneyAmount > 0) {
            payer.takeMoney(moneyAmount);
            recipient.addMoney(moneyAmount);
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    List.of(MoneyState.fromPlayer(payer), MoneyState.fromPlayer(recipient))));
        }
    }

    private void processOfferFieldExchange(List<PurchasableField> fields, Player newOwner) {
        if (!CollectionUtils.isEmpty(fields)) {
            for (PurchasableField field : fields) {
                field.newOwner(newOwner);
            }
            gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(gameFieldConverter.toListView(fields)));
        }
    }

    private void checkOfferNotEmpty(DealOffer offer) {
        Integer moneyToGive = offer.getMoneyToGive();
        Integer moneyToReceive = offer.getMoneyToReceive();
        if ((moneyToGive == null || moneyToGive == 0)
        && (moneyToReceive == null || moneyToReceive == 0)
        && CollectionUtils.isEmpty(offer.getFieldsToBuy())
        && CollectionUtils.isEmpty(offer.getFieldsToSell())) {
            throw new IllegalArgumentException("Cannot send an empty offer");
        }
    }
}
