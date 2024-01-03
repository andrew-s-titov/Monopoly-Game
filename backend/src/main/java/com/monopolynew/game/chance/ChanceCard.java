package com.monopolynew.game.chance;

import com.monopolynew.game.Game;
import com.monopolynew.service.GameEventSender;

public interface ChanceCard {

    GoTo apply(Game game, GameEventSender gameEventSender);
}
