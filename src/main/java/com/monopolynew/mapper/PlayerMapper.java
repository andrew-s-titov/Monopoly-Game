package com.monopolynew.mapper;

import com.monopolynew.dto.PlayerShortInfo;
import com.monopolynew.game.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlayerMapper {

    @Mapping(target = "playerId", source = "id")
    PlayerShortInfo toPlayerShortInfo(Player player);

    List<PlayerShortInfo> toPlayersShortInfoList(Collection<Player> players);
}
