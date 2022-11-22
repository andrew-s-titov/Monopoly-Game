package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PaymentProcessor {

    void createPayCheck(Game game, @NonNull Player player, @Nullable Player beneficiary, int amount, String paymentComment);

    void processPayment(Game game);
}