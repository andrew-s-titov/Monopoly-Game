package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;

public interface PaymentProcessor {

    void createPayCheck(Game game, Player player, Player beneficiary, int amount, String paymentComment);

    void processPayment(Game game);
}