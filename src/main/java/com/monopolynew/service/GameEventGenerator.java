package com.monopolynew.service;

import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.game.Game;

public interface GameEventGenerator {

    GameMapRefreshEvent newMapRefreshEvent(Game game);

    OfferProposalEvent newOfferProposalEvent(Game game);
}
