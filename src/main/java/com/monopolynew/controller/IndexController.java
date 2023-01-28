package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class IndexController {

    private final GameService gameService;

    @Value("${proxy.host}")
    private String proxyHost;

    @GetMapping
    public String homePage(Model model, HttpServletResponse response,
                           @CookieValue(value = GlobalConfig.PLAYER_ID_KEY, required = false) String playerIdCookie) {
        model.addAttribute("gameStarted", gameService.isGameStarted());
        model.addAttribute("proxyHost", proxyHost);
        if (gameService.isGameStarted() && playerIdCookie != null && !playerIdCookie.equalsIgnoreCase("null")) {
            model.addAttribute("needToReconnect", gameService.getPlayerName(playerIdCookie));
        } else {
            // TODO: remove upon login feature implementation
            Cookie cookie = new Cookie(GlobalConfig.PLAYER_ID_KEY, UUID.randomUUID().toString());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "index";
    }
}