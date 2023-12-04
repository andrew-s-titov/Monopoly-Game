package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.service.ChanceContainer;
import com.monopolynew.service.ChanceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class ChanceExecutorImpl implements ChanceExecutor {

    private final ChanceContainer chanceContainer;

    private final Map<UUID, Queue<Consumer<Game>>> chanceDecks = new ConcurrentHashMap<>();

    @Override
    public void executeChance(Game game) {
        Queue<Consumer<Game>> chanceDeck = getDeck(game);
        Consumer<Game> selectedChance = chanceDeck.poll();
        if (selectedChance == null) {
            throw new IllegalStateException("unexpectedly null chance (game consumer)");
        }
        selectedChance.accept(game);
    }

    private Queue<Consumer<Game>> getDeck(Game game) {
        UUID gameId = game.getId();
        Queue<Consumer<Game>> chanceDeck = chanceDecks.get(gameId);
        if (chanceDeck == null || chanceDeck.isEmpty()) {
            var newDeck = new LinkedList<>(chanceContainer.getChances());
            Collections.shuffle(newDeck);
            chanceDeck = newDeck;
            chanceDecks.put(gameId, chanceDeck);
        }
        return chanceDeck;
    }
}