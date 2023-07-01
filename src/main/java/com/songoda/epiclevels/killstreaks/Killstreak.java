package com.songoda.epiclevels.killstreaks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// TODO: Change name to KillStream
public class Killstreak {
    private final int killStreak;
    private final List<String> rewards;

    public Killstreak(int killStreak, List<String> rewards) {
        this.killStreak = killStreak;
        this.rewards = rewards;
    }

    public int getKillStreak() {
        return this.killStreak;
    }

    /**
     * @deprecated Use {@link #getKillStreak()} instead.
     */
    public int getKillstreak() {
        return getKillStreak();
    }

    public List<String> getRewards() {
        return Collections.unmodifiableList(this.rewards);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Killstreak killStreak1 = (Killstreak) obj;
        return this.killStreak == killStreak1.killStreak &&
                this.rewards.equals(killStreak1.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.killStreak, this.rewards);
    }
}
