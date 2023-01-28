package com.monopolynew.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopolynew.dto.PlayerMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatMessageEvent implements GameEvent {

    private final int code = 200;

    @JsonProperty("player_id")
    private final String playerId;

    private final String message;

    public static ChatMessageEvent fromPlayerMessage(PlayerMessageDTO playerMessageDTO) {
        return new ChatMessageEvent(playerMessageDTO.getPlayerId(), playerMessageDTO.getMessage());
    }
}