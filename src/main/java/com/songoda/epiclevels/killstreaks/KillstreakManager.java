package com.songoda.epiclevels.killstreaks;

import com.songoda.core.configuration.Config;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillstreakManager {
    private final Config killstreaksConfig = new Config(EpicLevels.getInstance(), "KillstreakRewards.yml");

    private static final Map<Integer, Killstreak> killstreaks = new HashMap<>();

    public void load(EpicLevels plugin) {
        killstreaksConfig.load();
        killstreaks.clear();

        if (!new File(plugin.getDataFolder(), "KillstreakRewards.yml").exists())
            EpicLevels.getInstance().saveResource("KillstreakRewards.yml", false);
        FileConfiguration killstreaksConfig = this.killstreaksConfig.getFileConfig();
        for (String key : killstreaksConfig.getKeys(false)) {
            int killstreak = Integer.parseInt(key);
            killstreaks.put(Integer.parseInt(key), new Killstreak(killstreak, killstreaksConfig.getStringList(String.valueOf(killstreak))));
        }
    }

    public List<Killstreak> getKillstreaks() {
        return new ArrayList<>(killstreaks.values());
    }

    public Killstreak getKillstreak(int killstreak) {
        return killstreaks.get(killstreak);
    }

    public Config getKillstreaksConfig() {
        return killstreaksConfig;
    }
}
