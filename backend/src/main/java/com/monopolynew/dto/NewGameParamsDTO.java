package com.monopolynew.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class NewGameParamsDTO {

    private Integer minPlayers;

    private Integer maxPlayers;

    private boolean withTeleport;

    private String language = "en";
}
