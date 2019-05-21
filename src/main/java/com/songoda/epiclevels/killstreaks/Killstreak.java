package com.songoda.epiclevels.killstreaks;

import java.util.Collections;
import java.util.List;

public class Killstreak {

    private final int killstreak;
    private List<String> rewards;

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
}
