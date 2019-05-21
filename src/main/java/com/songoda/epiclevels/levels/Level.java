package com.songoda.epiclevels.levels;

import java.util.Collections;
import java.util.List;

public class Level {

    private final int level;
    private List<String> rewards;

    public Level(int level, List<String> rewards) {
        this.level = level;
        this.rewards = rewards;
    }

    public int getLevel() {
        return level;
    }

    public List<String> getRewards() {
        return Collections.unmodifiableList(rewards);
    }
}
