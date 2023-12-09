package com.monopolynew.service.api;

import com.monopolynew.dto.DealOffer;
import com.monopolynew.enums.JailAction;
import com.monopolynew.enums.ProposalAction;
import org.springframework.lang.NonNull;

public interface GameService {

    boolean isGameStarted();

    boolean usernameTaken(String username);

    String getPlayerName(String playerId);

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

    void giveUp(String playerId);

    void mortgageField(int fieldIndex, String playerId);

    void redeemMortgagedProperty(int fieldIndex, String playerId);

    void buyHouse(int fieldIndex, String playerId);

    void sellHouse(int fieldIndex, String playerId);

    void createOffer(String offerInitiatorId, String offerAddresseeId, DealOffer offer);

    void processOfferAnswer(String callerId, ProposalAction proposalAction);
}