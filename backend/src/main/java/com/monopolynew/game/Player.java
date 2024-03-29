package com.monopolynew.game;

import com.monopolynew.user.GameUser;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

public class Player {

    @Getter
    private final UUID id;
    @Getter
    private final String name;
    @Getter
    private final String avatar;

    @Getter
    private int money = Rules.START_MONEY_AMOUNT;
    @Getter
    private int position = 0;
    @Getter
    private boolean bankrupt = false;
    private boolean amnestied = false;
    private int skipsTurns = 0;
    private int jailTurns = 0;
    @Getter
    private int doubletCount = 0;

    @Builder
    public Player(UUID id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    public static Player fromUser(GameUser user) {
        return Player.builder()
                .id(user.getId())
                .name(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

    public void changePosition(int newPosition) {
        this.position = newPosition;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    /**
     * If not enough money - take as much as possible
     *
     * @param amount of money to take
     * @return money taken
     */
    public int takeMoney(int amount) {
        boolean enoughMoney = money >= amount;
        money = enoughMoney ? money - amount : 0;
        return enoughMoney ? amount : money;
    }

    public void skipTurns(int skippingAmount) {
        this.skipsTurns = skippingAmount;
    }

    public boolean isSkipping() {
        return skipsTurns > 0;
    }

    public void skip() {
        if (skipsTurns < 1) {
            throw new IllegalStateException(name + " wasn't skipping");
        }
        skipsTurns--;
    }

    public void imprison() {
        this.jailTurns = Rules.TURNS_IN_JAIL;
    }

    public boolean isImprisoned() {
        return this.jailTurns > 0;
    }

    public boolean lastTurnInPrison() {
        return jailTurns == 1;
    }

    public void doTime() {
        if (jailTurns < 1) {
            throw new IllegalStateException(name + " wasn't in jail");
        }
        jailTurns--;
    }

    public void releaseFromJail() {
        this.jailTurns = 0;
    }

    public void amnesty() {
        this.jailTurns = 0;
        this.amnestied = true;
    }

    /**
     * Defines if player was released from prison because of doublet on dice throw
     *
     * @return true if player was released from prison because of doublet on dice throw, false if not.
     * If true is returned, next invocation will return false (the flag is dropped)
     */
    public boolean isJustAmnestied() {
        boolean result = this.amnestied;
        if (amnestied) {
            this.amnestied = false;
        }
        return result;
    }

    public void incrementDoublets() {
        this.doubletCount++;
    }

    public void resetDoublets() {
        this.doubletCount = 0;
    }

    public void goBankrupt() {
        this.bankrupt = true;
    }

    public void resetState() {
        this.money = Rules.START_MONEY_AMOUNT;
        this.bankrupt = false;
        this.amnestied = false;
        this.position = 0;
        this.skipsTurns = 0;
        this.jailTurns = 0;
        this.doubletCount = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return getId().equals(player.getId()) && getName().equals(player.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}