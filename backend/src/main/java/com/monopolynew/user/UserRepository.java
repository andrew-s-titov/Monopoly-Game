package com.monopolynew.user;

import com.monopolynew.exception.ClientBadRequestException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.monopolynew.util.CommonUtils.requireNotNullArgs;

@RequiredArgsConstructor
@Component
public class UserRepository {

    private final Map<UUID, GameUser> users = new HashMap<>();

    @NotNull
    public GameUser createUser(@NotNull String name, @NotNull String avatar) {
        requireNotNullArgs(name, avatar);

        var newUser = GameUser.builder()
                .name(name)
                .avatar(avatar)
                .build();
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @NotNull
    public GameUser getUser(UUID userId) {
        GameUser gameUser = users.get(userId);
        if (gameUser == null) {
            throw new ClientBadRequestException("User not found by userId");
        }
        return gameUser;
    }
}
