package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.service.api.ChanceCard;
import com.monopolynew.service.api.ChanceCardProvider;
import com.monopolynew.service.api.ChanceContainer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class ChanceCardProviderImpl implements ChanceCardProvider {

    private final ChanceContainer chanceContainer;

    private final Map<UUID, Queue<ChanceCard>> chanceDecks = new ConcurrentHashMap<>();

    @Override
    public void applyNextCard(Game game) {
        Queue<ChanceCard> chanceDeck = getDeck(game);
        ChanceCard selectedChance = chanceDeck.poll();
        if (selectedChance == null) {
            throw new IllegalStateException("unexpectedly null chance (game consumer)");
        }
        selectedChance.accept(game);
    }

    private Queue<ChanceCard> getDeck(Game game) {
        UUID gameId = game.getId();
        Queue<ChanceCard> chanceDeck = chanceDecks.get(gameId);
        if (chanceDeck == null || chanceDeck.isEmpty()) {
            chanceDeck = initiateCardDeck(gameId);
        }
        return chanceDeck;
    }

    private Queue<ChanceCard> initiateCardDeck(UUID gameId) {
        var newDeck = new LinkedList<>(chanceContainer.getChances());
        Collections.shuffle(newDeck);
        chanceDecks.put(gameId, newDeck);
        return newDeck;
    }
}