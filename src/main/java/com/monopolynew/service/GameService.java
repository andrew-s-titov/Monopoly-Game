package com.monopolynew.service;

import com.monopolynew.enums.ProposalAction;
import com.monopolynew.enums.JailAction;

public interface GameService {

    boolean isGameStarted();

    boolean usernameTaken(String username);

    String getPlayerName(String playerId);

    void startGame();

    void startRolling();

    void doRollTheDice();

    void afterDiceAction();

    void processBuyProposal(ProposalAction action);

    void processAuctionBuyProposal(ProposalAction action);

    void processAuctionRaiseProposal(ProposalAction action);

    void processJailAction(JailAction jailAction);
}