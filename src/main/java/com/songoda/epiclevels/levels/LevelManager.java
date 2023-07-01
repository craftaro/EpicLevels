package com.songoda.epiclevels.levels;

import com.songoda.core.configuration.Config;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {
    private final Plugin plugin;
    private final Config levelsConfig;

    private static final Map<Integer, Level> LEVELS = new HashMap<>();

    public LevelManager(Plugin plugin) {
        this.plugin = plugin;
        this.levelsConfig = new Config(this.plugin, "LevelUpRewards.yml");
    }

    public void load(EpicLevels plugin) {
        this.levelsConfig.load();
        LEVELS.clear();

        if (!new File(plugin.getDataFolder(), "LevelUpRewards.yml").exists()) {
            this.plugin.saveResource("LevelUpRewards.yml", false);
        }

        FileConfiguration levelsConfig = this.levelsConfig.getFileConfig();
        for (String key : levelsConfig.getKeys(false)) {
            int level = Integer.parseInt(key);
            LEVELS.put(Integer.parseInt(key), new Level(level, levelsConfig.getStringList(String.valueOf(level))));
        }
    }

    public List<Level> getLevels() {
        return new ArrayList<>(LEVELS.values());
    }

    public Level getLevel(int level) {
        return LEVELS.get(level);
    }

    public Config getLevelsConfig() {
        return this.levelsConfig;
    }
}
