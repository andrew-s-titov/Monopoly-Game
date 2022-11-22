package com.monopolynew.controller;

import com.monopolynew.config.GlobalConfig;
import com.monopolynew.enums.FieldManagementAction;
import com.monopolynew.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game/field")
public class FieldManagementController {

    private final GameService gameService;

    @GetMapping("/{fieldId}/management")
    public List<FieldManagementAction> availableManagementActions(@PathVariable("fieldId") Integer fieldId,
                                                                  @CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        return gameService.availableManagementActions(fieldId, playerId);
    }

    @GetMapping("/{fieldId}/mortgage")
    public void mortgageProperty(@PathVariable("fieldId") Integer fieldId,
                                 @CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        gameService.mortgageField(fieldId, playerId);
    }

    @GetMapping("/{fieldId}/redeem")
    public void redeemProperty(@PathVariable("fieldId") Integer fieldId,
                               @CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        gameService.redeemMortgagedProperty(fieldId, playerId);
    }

    @GetMapping("/{fieldId}/buy_house")
    public void buyHouse(@PathVariable("fieldId") Integer fieldId,
                         @CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        gameService.buyHouse(fieldId, playerId);
    }

    @GetMapping("/{fieldId}/sell_house")
    public void sellHouse(@PathVariable("fieldId") Integer fieldId,
                          @CookieValue(GlobalConfig.PLAYER_ID_KEY) String playerId) {
        gameService.sellHouse(fieldId, playerId);
    }
}
