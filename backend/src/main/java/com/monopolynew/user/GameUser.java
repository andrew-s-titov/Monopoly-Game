package com.monopolynew.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameUser {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String name;

    private String avatar;
}
