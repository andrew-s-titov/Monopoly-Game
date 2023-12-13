package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.service.api.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game/field")
public class FieldController {

    private final GameService gameService;

    @GetMapping("/{fieldIndex}/mortgage")
    public void mortgageProperty(@PathVariable("fieldIndex") Integer fieldIndex,
                                 @RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.mortgageField(fieldIndex, playerId);
    }

    @GetMapping("/{fieldIndex}/redeem")
    public void redeemProperty(@PathVariable("fieldIndex") Integer fieldIndex,
                               @RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.redeemMortgagedProperty(fieldIndex, playerId);
    }

    @GetMapping("/{fieldIndex}/buy_house")
    public void buyHouse(@PathVariable("fieldIndex") Integer fieldIndex,
                         @RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.buyHouse(fieldIndex, playerId);
    }

    @GetMapping("/{fieldIndex}/sell_house")
    public void sellHouse(@PathVariable("fieldIndex") Integer fieldIndex,
                          @RequestHeader(GlobalConfig.USER_ID_HEADER) UUID playerId) {
        gameService.sellHouse(fieldIndex, playerId);
    }
}
