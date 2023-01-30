package com.monopolynew.service.impl;

import com.monopolynew.event.GameMapRefreshEvent;
import com.monopolynew.event.OfferProposalEvent;
import com.monopolynew.game.Game;
import com.monopolynew.service.GameEventGenerator;
import com.monopolynew.service.GameFieldConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameEventGeneratorImpl implements GameEventGenerator {

    private final GameFieldConverter gameFieldConverter;

    @Override
    public GameMapRefreshEvent newMapRefreshEvent(Game game) {
        var gameFieldViews = gameFieldConverter.toListView(game.getGameMap().getFields());
        return new GameMapRefreshEvent(game.getPlayers(), gameFieldViews, game.getCurrentPlayer().getId());
    }

    @Override
    public OfferProposalEvent newOfferProposalEvent(Game game) {
        var offer = game.getOffer();
        var moneyToGive = offer.getMoneyToGive();
        var moneyToReceive = offer.getMoneyToReceive();

        var currentPlayer = game.getCurrentPlayer();
        return OfferProposalEvent.builder()
                .initiatorName(currentPlayer.getName())
                .fieldsToBuy(gameFieldConverter.toListOfferView(offer.getFieldsToBuy()))
                .fieldsToSell(gameFieldConverter.toListOfferView(offer.getFieldsToSell()))
                .moneyToGive(moneyToGive)
                .moneyToReceive(moneyToReceive)
                .build();
    }
}
