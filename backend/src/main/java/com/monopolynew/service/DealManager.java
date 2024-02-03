package com.monopolynew.service;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.GameFieldState;
import com.monopolynew.dto.MoneyState;
import com.monopolynew.enums.GameStage;
import com.monopolynew.enums.ProposalAction;
import com.monopolynew.event.FieldStateChangeEvent;
import com.monopolynew.event.JailReleaseProcessEvent;
import com.monopolynew.event.MoneyChangeEvent;
import com.monopolynew.event.SystemMessageEvent;
import com.monopolynew.event.TurnStartEvent;
import com.monopolynew.exception.ClientBadRequestException;
import com.monopolynew.exception.UserInvalidInputException;
import com.monopolynew.exception.WrongGameStageException;
import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import com.monopolynew.game.Rules;
import com.monopolynew.game.procedure.Offer;
import com.monopolynew.map.GameMap;
import com.monopolynew.map.PurchasableField;
import com.monopolynew.map.StreetField;
import com.monopolynew.mapper.GameFieldMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class DealManager {

    private final GameEventGenerator gameEventGenerator;
    private final GameFieldMapper gameFieldMapper;
    private final GameEventSender gameEventSender;
    private final GameLogicExecutor gameLogicExecutor;

    public void createOffer(Game game, UUID initiatorId, UUID addresseeId, DealOffer offer) {
        var gameId = game.getId();
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
        gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.deal.offer", Map.of(
                "initiator", currentPlayer.getName(),
                "addressee", offerAddressee.getName())));
        gameEventSender.sendToPlayer(gameId, addresseeId, gameEventGenerator.offerProposalEvent(game));
    }

    public void processOfferAnswer(Game game, UUID addresseeId, ProposalAction proposalAction) {
        var gameId = game.getId();
        var currentGameStage = game.getStage();
        if (!GameStage.DEAL_OFFER.equals(currentGameStage)) {
            throw new WrongGameStageException("Cannot process offer - wrong game stage");
        }
        var offer = game.getOffer();
        var addressee = offer.getAddressee();
        if (!addressee.getId().equals(addresseeId)) {
            // for security reasons
            throw new ClientBadRequestException("Only offer addressee can process offer");
        }
        var initiator = offer.getInitiator();
        if (ProposalAction.DECLINE.equals(proposalAction)) {
            gameEventSender.sendToAllPlayers(gameId, new SystemMessageEvent("event.deal.declined", Map.of(
                    "addressee", addressee.getName())));
        } else {
            gameEventSender.sendToAllPlayers(gameId, createOfferAcceptMessage(offer));
            processOfferPayments(gameId, offer, initiator, addressee);
            processOfferPropertyExchange(game, offer, initiator, addressee);
        }
        game.setOffer(null);
        GameStage stageToReturnTo = offer.getStageToReturnTo();
        gameLogicExecutor.changeGameStage(game, stageToReturnTo);
        var initiatorId = initiator.getId();
        if (GameStage.TURN_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(gameId, initiatorId, new TurnStartEvent());
        }
        if (GameStage.JAIL_RELEASE_START.equals(stageToReturnTo)) {
            gameEventSender.sendToPlayer(gameId, initiatorId, new JailReleaseProcessEvent());
        }
    }

    private void checkDealSides(Game game, UUID offerInitiatorId, UUID offerAddresseeId) {
        checkOfferInitiator(game, offerInitiatorId);
        getOfferAddressee(game, offerAddresseeId);
    }

    private void checkCanCreateOffer(Game game) {
        var currentGameStage = game.getStage();
        if (!currentGameStage.equals(GameStage.TURN_START) && !currentGameStage.equals(GameStage.JAIL_RELEASE_START)) {
            throw new WrongGameStageException("cannot start deal process - wrong game stage");
        }
    }

    private void checkOfferInitiator(Game game, UUID offerInitiatorId) {
        if (!game.getCurrentPlayer().getId().equals(offerInitiatorId)) {
            throw new ClientBadRequestException("only current player can initiate deal process");
        }
    }

    private Player getOfferAddressee(Game game, UUID offerAddresseeId) {
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
            throw new UserInvalidInputException(message);
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
        if (CollectionUtils.isNotEmpty(fields) && fields.stream()
                .filter(StreetField.class::isInstance)
                .map(StreetField.class::cast)
                .anyMatch(field -> field.getHouses() > 0)) {
            throw new ClientBadRequestException("Selling fields with property is not allowed");
        }
    }

    private void processOfferPayments(UUID gameId, Offer offer, Player initiator, Player addressee) {
        var initiatorMoney = offer.getInitiatorMoney();
        var addresseeMoney = offer.getAddresseeMoney();
        if (processOfferPayment(initiatorMoney, initiator, addressee) ||
                processOfferPayment(addresseeMoney, addressee, initiator)) {
            gameEventSender.sendToAllPlayers(gameId, new MoneyChangeEvent(
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

        List<PurchasableField> processedOwnedFields = gameLogicExecutor.processOwnershipChange(game, exchangedFields);
        List<GameFieldState> newFieldStates = gameFieldMapper.toStateList(processedOwnedFields);
        gameEventSender.sendToAllPlayers(game.getId(), new FieldStateChangeEvent(newFieldStates));
    }

    private boolean processOfferFieldExchange(List<PurchasableField> fields, Player newOwner) {
        if (CollectionUtils.isNotEmpty(fields)) {
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
            throw new UserInvalidInputException("Cannot send an empty offer");
        }
    }

    private SystemMessageEvent createOfferAcceptMessage(Offer offer) {
        var initiatorResult = buildDealSideResult(offer, Offer::getInitiatorFields, Offer::getInitiatorMoney);
        var addresseeResult = buildDealSideResult(offer, Offer::getAddresseeFields, Offer::getAddresseeMoney);
        var translationKey = defineDealResultTranslationKey(initiatorResult, addresseeResult);
        return new SystemMessageEvent(translationKey, Map.of(
                "initiator", offer.getInitiator().getName(),
                "initiatorResult", initiatorResult,
                "addressee", offer.getAddressee().getName(),
                "addresseeResult", addresseeResult));
    }

    private List<String> buildDealSideResult(Offer offer, Function<Offer, List<PurchasableField>> fieldsGetter,
                                             Function<Offer, Integer> moneyGetter) {
        Integer sideMoney = moneyGetter.apply(offer);
        var sideResult = new ArrayList<>(fieldsGetter.apply(offer).stream()
                .map(PurchasableField::getName)
                .toList());
        if (sideMoney != null && sideMoney > 0) {
            sideResult.add("$" + sideMoney);
        }
        return sideResult;
    }

    private String defineDealResultTranslationKey(List<String> initiatorResult, List<String> addresseeResult) {
        if (CollectionUtils.isNotEmpty(initiatorResult) && CollectionUtils.isNotEmpty(addresseeResult)) {
            return "event.deal.bothGet";
        } else if (CollectionUtils.isNotEmpty(initiatorResult)) {
            return "event.deal.initiatorGets";
        } else {
            return "event.deal.addresseeGets";
        }
    }
}
