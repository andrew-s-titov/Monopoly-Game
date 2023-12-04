package com.monopolynew.service.impl;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.GameFieldView;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.ChatMessageEvent;
import com.monopolynew.event.FieldViewChangeEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.OfferProcessedEvent;
import com.monopolynew.event.OfferSentEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.PlayerInvalidInputException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.state.Offer;
import com.monopolynew.map.CompanyField;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.PurchasableFieldGroups;
import com.monopolynew.map.StreetField;
import com.monopolynew.map.UtilityField;
import com.monopolynew.service.DealManager;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameEventSender;
import com.monopolynew.service.GameFieldConverter;
import com.monopolynew.service.GameLogicExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DealManagerImpl implements DealManager {

    private final GameEventGenerator gameEventGenerator;
    private final GameFieldConverter gameFieldConverter;
    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    @Override
    public void createOffer(Game game, String initiatorId, String addresseeId, DealOffer offer) {
        checkCanCreateOffer(game);
        checkDealSides(game, initiatorId, addresseeId);
        var currentPlayer = game.getCurrentPlayer();
        var offerAddressee = getOfferAddressee(game, addresseeId);
        checkOfferNotEmpty(offer);
        var initiatorMoney = offer.getInitiatorMoney();
        var addresseeMoney = offer.getAddresseeMoney();
        checkPlayerSolvency(currentPlayer, initiatorMoney, true);
        checkPlayerSolvency(offerAddressee, addresseeMoney, false);
        var initiatorFields = getFieldsByIndexes(game, offer.getInitiatorFields());
        var addresseeFields = getFieldsByIndexes(game, offer.getAddresseeFields());
        checkFieldsOwner(initiatorFields, currentPlayer);
        checkFieldsOwner(addresseeFields, offerAddressee);
        checkNoHouses(initiatorFields);
        checkNoHouses(addresseeFields);
        var currentGameStage = game.getStage();
        gameLogicExecutor.changeGameStage(game, GameStage.DEAL_OFFER);

        var newOffer = Offer.builder()
                .initiator(currentPlayer)
                .addressee(offerAddressee)
                .initiatorFields(initiatorFields)
                .addresseeFields(addresseeFields)
                .initiatorMoney(initiatorMoney)
                .addresseeMoney(addresseeMoney)
                .stageToReturnTo(currentGameStage)
                .build();
        game.setOffer(newOffer);
        gameEventSender.sendToAllPlayers(new ChatMessageEvent(
                String.format("%s offered %s a deal", currentPlayer.getName(), offerAddressee.getName())));
        gameEventSender.sendToPlayer(initiatorId, new OfferSentEvent());
        gameEventSender.sendToPlayer(addresseeId, gameEventGenerator.newOfferProposalEvent(game));
    }

    @Override
    public void processOfferAnswer(Game game, String addresseeId, ProposalAction proposalAction) {
        var currentGameStage = game.getStage();
        if (!GameStage.DEAL_OFFER.equals(currentGameStage)) {
            throw new WrongGameStageException("cannot process offer - wrong game stage");
        }
        var offer = game.getOffer();
        var addressee = offer.getAddressee();
        if (!addressee.getId().equals(addresseeId)) {
            // for security reasons
            throw new ClientBadRequestException("only offer addressee can process offer");
        }
        var initiator = offer.getInitiator();
        if (ProposalAction.DECLINE.equals(proposalAction)) {
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(addressee.getName() + " declined the offer"));
        } else {
            gameEventSender.sendToAllPlayers(new ChatMessageEvent(addressee.getName() + " accepted the offer"));
            processOfferPayments(offer, initiator, addressee);
            processOfferPropertyExchange(game, offer, initiator, addressee);
        }
        game.setOffer(null);
        GameStage stageToReturnTo = offer.getStageToReturnTo();
        gameLogicExecutor.changeGameStage(game, stageToReturnTo);
        String offerInitiatorId = initiator.getId();
        gameEventSender.sendToPlayer(offerInitiatorId, new OfferProcessedEvent());
        if (GameStage.TURN_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(offerInitiatorId, new TurnStartEvent(offerInitiatorId));
        }
        if (GameStage.JAIL_RELEASE_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(offerInitiatorId,
                    new JailReleaseProcessEvent(offerInitiatorId, initiator.getMoney() >= Rules.JAIL_BAIL));
        }
    }

    private void checkDealSides(Game game, String offerInitiatorId, String offerAddresseeId) {
        checkOfferInitiator(game, offerInitiatorId);
        getOfferAddressee(game, offerAddresseeId);
    }

    private void checkCanCreateOffer(Game game) {
        var currentGameStage = game.getStage();
        if (!currentGameStage.equals(GameStage.TURN_START) && !currentGameStage.equals(GameStage.JAIL_RELEASE_START)) {
            throw new WrongGameStageException("cannot start deal process - wrong game stage");
        }
    }

    private void checkOfferInitiator(Game game, String offerInitiatorId) {
        if (!game.getCurrentPlayer().getId().equals(offerInitiatorId)) {
            throw new ClientBadRequestException("only current player can initiate deal process");
        }
    }

    private Player getOfferAddressee(Game game, String offerAddresseeId) {
        var offerAddressee = game.getPlayerById(offerAddresseeId);
        if (offerAddressee == null) {
            throw new ClientBadRequestException(String.format("player with id %s doesn't exist", offerAddresseeId));
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
                    .toList();
        }
    }

    private PurchasableField safeGetFieldByIndex(GameMap gameMap, Integer index) {
        if (index < 0 || index > Rules.LAST_FIELD_INDEX) {
            throw new ClientBadRequestException("Field index must be from 0 to " + Rules.LAST_FIELD_INDEX);
        }
        var gameField = gameMap.getField(index);
        if (!(gameField instanceof PurchasableField)) {
            throw new ClientBadRequestException("Field with index " + index + " cannot be a subject for a deal");
        }
        return (PurchasableField) gameField;
    }

    private void checkPlayerSolvency(Player player, Integer money, boolean initiator) {
        if (money != null && player.getMoney() < money) {
            var message = String.format("%s cannot afford this deal", initiator ? "You" : player.getName());
            throw new PlayerInvalidInputException(message);
        }
    }

    private void checkFieldsOwner(List<PurchasableField> fields, Player owner) {
        for (PurchasableField field : fields) {
            if (!owner.equals(field.getOwner())) {
                throw new ClientBadRequestException(
                        String.format("field %s doesn't belong to the player %s", field.getId(), owner.getId()));
            }
        }
    }

    private void checkNoHouses(List<PurchasableField> fields) {
        if (!CollectionUtils.isEmpty(fields) && fields.stream()
                .filter(StreetField.class::isInstance)
                .map(StreetField.class::cast)
                .anyMatch(field -> field.getHouses() > 0)) {
            throw new ClientBadRequestException("Selling fields with property is not allowed");
        }
    }

    private void processOfferPayments(Offer offer, Player initiator, Player addressee) {
        var initiatorMoney = offer.getInitiatorMoney();
        var addresseeMoney = offer.getAddresseeMoney();
        if (processOfferPayment(initiatorMoney, initiator, addressee) ||
                processOfferPayment(addresseeMoney, addressee, initiator)) {
            gameEventSender.sendToAllPlayers(new MoneyChangeEvent(
                    List.of(MoneyState.fromPlayer(initiator), MoneyState.fromPlayer(addressee))));
        }
    }

    private boolean processOfferPayment(Integer moneyAmount, Player payer, Player recipient) {
        if (moneyAmount != null && moneyAmount > 0) {
            payer.takeMoney(moneyAmount);
            recipient.addMoney(moneyAmount);
            return true;
        }
        return false;
    }

    private void processOfferPropertyExchange(Game game, Offer offer, Player initiator, Player addressee) {
        Set<PurchasableField> exchangedFields = new HashSet<>();
        var addresseeFields = offer.getAddresseeFields();
        if (processOfferFieldExchange(addresseeFields, initiator)) {
            exchangedFields.addAll(addresseeFields);
        }
        var initiatorFields = offer.getInitiatorFields();
        if (processOfferFieldExchange(initiatorFields, addressee)) {
            exchangedFields.addAll(initiatorFields);
        }

        Set<List<PurchasableField>> fieldGroupsToCheck = exchangedFields.stream()
                .map(field -> PurchasableFieldGroups.getGroupByFieldIndex(game, field.getId()))
                .collect(Collectors.toSet());
        List<GameFieldView> fieldViewsToSend = new ArrayList<>();
        fieldGroupsToCheck.forEach(group -> fieldViewsToSend.addAll(recalculateFieldGroupPrices(game, group)));

        if (!CollectionUtils.isEmpty(fieldViewsToSend)) {
            gameEventSender.sendToAllPlayers(new FieldViewChangeEvent(fieldViewsToSend));
        }
    }

    private List<GameFieldView> recalculateFieldGroupPrices(Game game, List<PurchasableField> fieldGroup) {
        var ownedFields = fieldGroup.stream()
                .filter(field -> !field.isFree())
                .toList();
        if (CollectionUtils.isEmpty(fieldGroup)) {
            return Collections.emptyList();
        }
        var anyGroupField = fieldGroup.get(0);
        if (anyGroupField instanceof StreetField) {
            boolean allGroupOwnedByTheSameOwner = fieldGroup.size() == ownedFields.size()
                    && 1 == fieldGroup.stream()
                    .map(PurchasableField::getOwner)
                    .distinct().count();
            ownedFields.stream()
                    .map(StreetField.class::cast)
                    .forEach(streetField -> streetField.setNewRent(allGroupOwnedByTheSameOwner));
        } else if (anyGroupField instanceof CompanyField) {
            ownedFields.forEach(ownedField -> {
                var owningPlayer = ownedField.getOwner();
                long ownedByTheSameOwner = ownedFields.stream()
                        .filter(field -> field.getOwner().equals(owningPlayer))
                        .count();
                ((CompanyField) ownedField).setNewRent((int) ownedByTheSameOwner);
            });
        } else if (anyGroupField instanceof UtilityField) {
            boolean increasedMultiplier = fieldGroup.size() == ownedFields.size()
                    && 1 == ownedFields.stream()
                    .map(PurchasableField::getOwner)
                    .distinct()
                    .count();
            ownedFields.stream()
                    .map(UtilityField.class::cast)
                    .forEach(field -> {
                        if (increasedMultiplier) {
                            field.increaseMultiplier();
                        } else {
                            field.decreaseMultiplier();
                        }
                    });
        } else {
            throw new IllegalStateException("Unsupported field type");
        }
        return gameFieldConverter.toListView(ownedFields);
    }

    private boolean processOfferFieldExchange(List<PurchasableField> fields, Player newOwner) {
        if (!CollectionUtils.isEmpty(fields)) {
            for (PurchasableField field : fields) {
                field.newOwner(newOwner);
            }
            return true;
        }
        return false;
    }

    private void checkOfferNotEmpty(DealOffer offer) {
        Integer moneyToGive = offer.getInitiatorMoney();
        Integer moneyToReceive = offer.getAddresseeMoney();
        if ((moneyToGive == null || moneyToGive == 0)
                && (moneyToReceive == null || moneyToReceive == 0)
                && CollectionUtils.isEmpty(offer.getAddresseeFields())
                && CollectionUtils.isEmpty(offer.getInitiatorFields())) {
            throw new PlayerInvalidInputException("Cannot send an empty offer");
        }
    }
}
