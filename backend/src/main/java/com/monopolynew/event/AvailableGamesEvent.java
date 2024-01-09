package com.monopolynew.event;

import com.monopolynew.dto.GameRoomParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableGamesEvent {

    private Map<UUID, List<GameRoomParticipant>> rooms;
}
