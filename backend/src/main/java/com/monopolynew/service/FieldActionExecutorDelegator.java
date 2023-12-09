package com.monopolynew.service;

import com.monopolynew.game.Game;
import com.monopolynew.map.FieldAction;
import com.monopolynew.service.fieldactionexecutors.api.FieldActionExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FieldActionExecutorDelegator {

    private final Map<FieldAction, FieldActionExecutor> fieldActionExecutors;

    public FieldActionExecutorDelegator(List<FieldActionExecutor> fieldActionExecutors) {
        this.fieldActionExecutors = fieldActionExecutors.stream()
                .collect(Collectors.toMap(FieldActionExecutor::getFieldAction, e -> e));
    }

    public void execute(FieldAction action, Game game) {
        FieldActionExecutor executor = fieldActionExecutors.get(action);
        if (executor == null) {
            throw new IllegalStateException("No executor found for " + action);
        }
        executor.executeAction(game);
    }
}
