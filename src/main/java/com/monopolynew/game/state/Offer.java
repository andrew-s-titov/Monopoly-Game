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

    private final List<PurchasableField> fieldsToBuy;

    private final List<PurchasableField> fieldsToSell;

    private final Integer moneyToGive;

    private final Integer moneyToReceive;

    private final GameStage stageToReturnTo;
}
