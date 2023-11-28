package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.exception.PlayerInvalidInputException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void homePage(@RequestParam(name = "name") String playerName,
                         HttpServletResponse response) {
        // TODO: remove upon login feature implementation
        if (StringUtils.isBlank(playerName) || playerName.length() < 3 || playerName.length() > 20) {
            throw new PlayerInvalidInputException("Player name length must be from 3 to 20 characters");
        }
        response.addCookie(newCookie(GlobalConfig.PLAYER_ID_KEY, UUID.randomUUID().toString()));
        response.addCookie(newCookie(GlobalConfig.PLAYER_NAME_KEY, playerName));
    }

    private Cookie newCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setPath("/");
        return cookie;
    }
}