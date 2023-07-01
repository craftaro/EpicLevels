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
        return this.level;
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

        Level level1 = (Level) obj;
        return this.level == level1.level && this.rewards.equals(level1.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.level, this.rewards);
    }
}
