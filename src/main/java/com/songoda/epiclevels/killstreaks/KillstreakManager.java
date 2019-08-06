package com.songoda.epiclevels.killstreaks;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.levels.Level;
import com.songoda.epiclevels.utils.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillstreakManager {

    private ConfigWrapper killstreaksFile = new ConfigWrapper(EpicLevels.getInstance(), "", "KillstreakRewards.yml");

    private static final Map<Integer, Killstreak> killstreaks = new HashMap<>();

    public void load() {
        killstreaksFile.reloadConfig();
        killstreaks.clear();
        EpicLevels.getInstance().saveResource("KillstreakRewards.yml", false);
        FileConfiguration killstreaksConfig = killstreaksFile.getConfig();
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
}
