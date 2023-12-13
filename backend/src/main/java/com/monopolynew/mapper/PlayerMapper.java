package com.monopolynew.mapper;

import com.monopolynew.dto.UserInfo;
import com.monopolynew.game.Player;
import com.monopolynew.user.GameUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlayerMapper {

    UserInfo toUserInfo(Player player);

    List<UserInfo> toUserInfoList(Collection<Player> players);

    UserInfo toUserInfo(GameUser gameUser);

    List<UserInfo> toUserInfoList(List<GameUser> gameUser);
}
