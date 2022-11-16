package com.monopolynew.service.impl;

import com.monopolynew.event.WebsocketEvent;
import com.monopolynew.game.Game;
import com.monopolynew.service.ChanceContainer;
import com.monopolynew.service.ChanceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class ChanceExecutorImpl implements ChanceExecutor {

    private final ChanceContainer chanceContainer;

    private final Random random = new Random();

    @Override
    public void executeRandomChance(Game game) {
        List<Function<Game, List<WebsocketEvent>>> chances = chanceContainer.getChances();
        chances.get(random.nextInt(chances.size())).apply(game);
    }
}
