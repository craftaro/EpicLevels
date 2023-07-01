package com.songoda.epiclevels.killstreaks;

import com.songoda.core.configuration.Config;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Change name to KillStreamManager
public class KillstreakManager {
    private final Plugin plugin;
    private final Config killstreaksConfig;

    private static final Map<Integer, Killstreak> KILL_STREAKS = new HashMap<>();

    public KillstreakManager(Plugin plugin) {
        this.plugin = plugin;
        this.killstreaksConfig = new Config(this.plugin, "KillstreakRewards.yml");
    }

    public void load(EpicLevels plugin) {
        this.killstreaksConfig.load();
        KILL_STREAKS.clear();

        if (!new File(plugin.getDataFolder(), "KillstreakRewards.yml").exists()) {
            this.plugin.saveResource("KillstreakRewards.yml", false);
        }

        FileConfiguration killStreaksConfig = this.killstreaksConfig.getFileConfig();
        for (String key : killStreaksConfig.getKeys(false)) {
            int killStreak = Integer.parseInt(key);
            KILL_STREAKS.put(Integer.parseInt(key), new Killstreak(killStreak, killStreaksConfig.getStringList(String.valueOf(killStreak))));
        }
    }

    public List<Killstreak> getKillStreaks() {
        return new ArrayList<>(KILL_STREAKS.values());
    }

    /**
     * @deprecated Use {@link #getKillStreaks()} instead.
     */
    public List<Killstreak> getKillstreaks() {
        return getKillStreaks();
    }

    public Killstreak getKillStreak(int killStreak) {
        return KILL_STREAKS.get(killStreak);
    }

    /**
     * @deprecated Use {@link #getKillStreak(int)} instead.
     */
    @Deprecated
    public Killstreak getKillstreak(int killStreak) {
        return getKillStreak(killStreak);
    }

    public Config getKillstreaksConfig() {
        return this.killstreaksConfig;
    }
}
