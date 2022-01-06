package com.songoda.epiclevels.killstreaks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Killstreak {
    private final int killstreak;
    private final List<String> rewards;

    public Killstreak(int killstreak, List<String> rewards) {
        this.killstreak = killstreak;
        this.rewards = rewards;
    }

    public int getKillstreak() {
        return killstreak;
    }

    public List<String> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Killstreak that = (Killstreak) o;
        return killstreak == that.killstreak && rewards.equals(that.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(killstreak, rewards);
    }
}
