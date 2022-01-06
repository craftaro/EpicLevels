package com.songoda.epiclevels.levels;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Level {

    private final int level;
    private final List<String> rewards;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level1 = (Level) o;
        return level == level1.level && rewards.equals(level1.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, rewards);
    }
}
