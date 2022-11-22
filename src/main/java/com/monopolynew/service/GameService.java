package com.monopolynew.service;

import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;

import java.util.List;

public interface GameService {

    boolean isGameStarted();

    boolean usernameTaken(String username);

    String getPlayerName(String playerId);

    void startGame();

    void startRolling();

    void doRollTheDice();

    void afterDiceRollAction();

    void processBuyProposal(ProposalAction action);

    void processAuctionBuyProposal(ProposalAction action);

    void processAuctionRaiseProposal(ProposalAction action);

    void processJailAction(JailAction jailAction);

    void processPayment();

    void giveUp();

    List<FieldManagementAction> availableManagementActions(int fieldId, String playerId);

    void mortgageField(int fieldId, String playerId);

    void redeemMortgagedProperty(int fieldId, String playerId);

    void buyHouse(int fieldId, String playerId);

    void sellHouse(int fieldId, String playerId);
}