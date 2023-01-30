package com.monopolynew.service;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.dto.PreDealInfo;
import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.PlayerManagementAction;
import com.monopolynew.enums.ProposalAction;

import java.util.List;

public interface GameService {

    boolean isGameStarted();

    boolean usernameTaken(String username);

    String getPlayerName(String playerId);

    void startGame();

    void startDiceRolling();

    void broadcastDiceResult();

    void afterDiceRollAction();

    void afterPlayerMoveAction();

    void processBuyProposal(ProposalAction action);

    void processAuctionBuyProposal(ProposalAction action);

    void processAuctionRaiseProposal(ProposalAction action);

    void processJailAction(JailAction jailAction);

    void processPayment();

    void giveUp(String playerId);

    List<FieldManagementAction> availableFieldManagementActions(int fieldIndex, String playerId);

    List<PlayerManagementAction> availablePlayerManagementActions(String requestingPlayerId, String subjectPlayerId);

    void mortgageField(int fieldIndex, String playerId);

    void redeemMortgagedProperty(int fieldIndex, String playerId);

    void buyHouse(int fieldIndex, String playerId);

    void sellHouse(int fieldIndex, String playerId);

    PreDealInfo getPreDealInfo(String offerInitiatorId, String offerAddresseeId);

    void createOffer(String offerInitiatorId, String offerAddresseeId, DealOffer offer);

    void processOfferAnswer(String callerId, ProposalAction proposalAction);
}