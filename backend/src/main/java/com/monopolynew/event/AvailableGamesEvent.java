package com.monopolynew.event;

import com.monopolynew.dto.GameRoomParticipant;
import com.monopolynew.game.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableGamesEvent {

    private Collection<AvailableGame> rooms;

    public static AvailableGamesEvent from(Collection<Game> games) {
        var availableGames = games.stream()
                .filter(game -> !game.isInProgress())
                .map(game -> new AvailableGame(
                        game.getId(),
                        game.getPlayers().stream()
                                .map(player -> new GameRoomParticipant(player.getName(), player.getAvatar()))
                                .toList(),
                        "en"))
                .toList();
        return new AvailableGamesEvent(availableGames);
    }

    public static AvailableGamesEvent empty() {
        return new AvailableGamesEvent(emptyList());
    }

    private record AvailableGame(
            UUID gameId,
            List<GameRoomParticipant> players,
            String language
    ) {
    }
}
