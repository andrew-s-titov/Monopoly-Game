package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String homePage(HttpServletResponse response,
                           @CookieValue(value = GlobalConfig.PLAYER_ID_KEY, required = false) String playerIdCookie) {
        if (playerIdCookie == null || playerIdCookie.equalsIgnoreCase("null")) {
            // TODO: remove upon login feature implementation
            Cookie cookie = new Cookie(GlobalConfig.PLAYER_ID_KEY, UUID.randomUUID().toString());
            cookie.setMaxAge(60 * 60 * 24 * 365);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "index";
    }
}