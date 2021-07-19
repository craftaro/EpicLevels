package com.songoda.epiclevels.levels;

import com.songoda.core.configuration.Config;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {
    private final Config levelsConfig = new Config(EpicLevels.getInstance(), "LevelUpRewards.yml");

    private static final Map<Integer, Level> levels = new HashMap<>();

    public void load(EpicLevels plugin) {
        levelsConfig.load();
        levels.clear();

        if (!new File(plugin.getDataFolder(), "LevelUpRewards.yml").exists())
            EpicLevels.getInstance().saveResource("LevelUpRewards.yml", false);

        FileConfiguration levelsConfig = this.levelsConfig.getFileConfig();
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

    public Config getLevelsConfig() {
        return levelsConfig;
    }
}
