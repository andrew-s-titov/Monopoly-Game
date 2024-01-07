package com.monopolynew.service.fieldactionexecutors;

import com.monopolynew.game.Game;
import com.monopolynew.game.chance.ChanceCard;
import com.monopolynew.game.chance.GoTo;
import com.monopolynew.service.GameEventSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.monopolynew.game.chance.ChanceContainer.CHANCES;

@RequiredArgsConstructor
@Component
public class ChanceExecutor {

    @Getter
    private final Map<UUID, Queue<ChanceCard>> chanceDecks = new ConcurrentHashMap<>();

    private final GameEventSender gameEventSender;

    public GoTo applyNextCard(Game game) {
        Queue<ChanceCard> chanceDeck = getDeck(game);
        ChanceCard selectedChance = chanceDeck.poll();
        if (selectedChance == null) {
            throw new IllegalStateException("unexpectedly null chance (game consumer)");
        }
        return selectedChance.apply(game, gameEventSender);
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
        var newDeck = new LinkedList<>(CHANCES);
        Collections.shuffle(newDeck);
        chanceDecks.put(gameId, newDeck);
        return newDeck;
    }
}
