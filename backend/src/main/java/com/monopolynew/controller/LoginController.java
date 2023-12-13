package com.monopolynew.controller;

import com.monopolynew.dto.LoginData;
import com.monopolynew.dto.UserInfo;
import com.monopolynew.exception.PlayerInvalidInputException;
import com.monopolynew.mapper.PlayerMapper;
import com.monopolynew.user.GameUser;
import com.monopolynew.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class LoginController {

    private final UserRepository userRepository;
    private final PlayerMapper playerMapper;

    @PostMapping
    public UserInfo createUser(@RequestBody LoginData loginData) {
        String playerName = loginData.getName();
        // TODO: remove upon login feature implementation
        if (StringUtils.isBlank(playerName) || playerName.length() < 3 || playerName.length() > 20) {
            throw new PlayerInvalidInputException("Player name length must be from 3 to 20 characters");
        }
        GameUser user = userRepository.createUser(playerName, loginData.getAvatar());
        return playerMapper.toUserInfo(user);
    }
}
