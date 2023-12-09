package com.monopolynew.service.api;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PaymentProcessor {

    void startPaymentProcess(Game game, @NonNull Player player, @Nullable Player beneficiary, int amount, String paymentComment);

    void processPayment(Game game);
}