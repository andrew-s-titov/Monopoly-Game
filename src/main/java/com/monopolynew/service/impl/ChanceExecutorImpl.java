package com.monopolynew.service.impl;

import com.monopolynew.game.Game;
import com.monopolynew.service.ChanceContainer;
import com.monopolynew.service.ChanceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class ChanceExecutorImpl implements ChanceExecutor {

    private final ChanceContainer chanceContainer;

    private final Random random = new Random(); // TODO: when multiple games are allowed - thread local?
    private final Map<UUID, List<Consumer<Game>>> chanceDecks = new ConcurrentHashMap<>();

    @Override
    public void executeRandomChance(Game game) {
        List<Consumer<Game>> chanceDeck = getDeck(game);
        int randomDeckIndex = random.nextInt(chanceDeck.size());
        Consumer<Game> selectedChance = chanceDeck.get(randomDeckIndex);
        chanceDeck.remove(randomDeckIndex);
        selectedChance.accept(game);
    }

    private List<Consumer<Game>> getDeck(Game game) {
        UUID gameId = game.getId();
        List<Consumer<Game>> chanceDeck = chanceDecks.get(gameId);
        if (chanceDeck == null || chanceDeck.isEmpty()) {
            chanceDeck = new ArrayList<>(chanceContainer.getChances());
            chanceDecks.put(gameId, chanceDeck);
        }
        return chanceDeck;
    }
}