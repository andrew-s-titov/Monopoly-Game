package com.monopolynew.game.state;

import com.monopolynew.enums.GameStage;
import com.monopolynew.game.Player;
import com.monopolynew.map.PurchasableField;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Builder
public class Offer {

    private final Player initiator;

    private final Player addressee;

    private final List<PurchasableField> addresseeFields;

    private final List<PurchasableField> initiatorFields;

    private final Integer initiatorMoney;

    private final Integer addresseeMoney;

    private final GameStage stageToReturnTo;
}
