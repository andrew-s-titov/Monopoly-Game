package com.monopolynew.service.api;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface GameService {

    boolean isGameStarted();

    void startGame();

    void startDiceRolling();

    void broadcastDiceResult();

    void afterDiceRollAction();

    void afterPlayerMoveAction();

    void processBuyProposal(@NonNull ProposalAction action);

    void processAuctionBuyProposal(ProposalAction action);

    void processAuctionRaiseProposal(ProposalAction action);

    void processJailAction(@NonNull JailAction jailAction);

    void processPayment();

    void giveUp(UUID playerId);

    void mortgageField(int fieldIndex, UUID playerId);

    void redeemMortgagedProperty(int fieldIndex, UUID playerId);

    void buyHouse(int fieldIndex, UUID playerId);

    void sellHouse(int fieldIndex, UUID playerId);

    void createOffer(UUID offerInitiatorId, UUID offerAddresseeId, DealOffer offer);

    void processOfferAnswer(UUID callerId, ProposalAction proposalAction);
}