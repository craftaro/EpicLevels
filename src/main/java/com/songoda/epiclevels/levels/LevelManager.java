package com.songoda.epiclevels.levels;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.utils.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private ConfigWrapper levelsFile = new ConfigWrapper(EpicLevels.getInstance(), "", "LevelUpRewards.yml");

    private static final Map<Integer, Level> levels = new HashMap<>();

    public void load() {
        EpicLevels.getInstance().saveResource("LevelUpRewards.yml", false);
        FileConfiguration levelsConfig = levelsFile.getConfig();
        for (String key : levelsConfig.getKeys(false)) {
            int level = Integer.parseInt(key);
            levels.put(Integer.parseInt(key), new Level(level, levelsConfig.getStringList(String.valueOf(level))));
        }
    }

    public List<Level> getLevels() {
        return new ArrayList<>(levels.values());
    }

    public Level getLevel(int level) {
        return levels.get(level);
    }
}
