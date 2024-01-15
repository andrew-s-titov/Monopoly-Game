package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.game.Player;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GameRepository gameRepository;

    @Nullable
    public UUID findActiveGameSession(UUID userId) {
        // TODO: get game id when multi-room setup is ready
        Optional<Pair<UUID, List<UUID>>> possibleGameEntry = gameRepository.allGames().stream()
                .filter(Game::isInProgress)
                .map(game -> Pair.of(game.getId(), game.getPlayers().stream().map(Player::getId).toList()))
                .filter(entry -> entry.getValue().contains(userId))
                .findAny();
        return possibleGameEntry.map(Pair::getKey).orElse(null);
    }
}
