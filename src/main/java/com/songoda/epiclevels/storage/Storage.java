package com.songoda.epiclevels.storage;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.ConfigWrapper;

import java.util.List;
import java.util.UUID;

public abstract class Storage {

    protected final EpicLevels instance;
    protected final ConfigWrapper dataFile;

    public Storage(EpicLevels instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicLevels Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicLevels instance) {
        // Save game data
        for (EPlayer ePlayer : instance.getPlayerManager().getPlayers()) {
            prepareSaveItem("players", new StorageItem("uuid", ePlayer.getUniqueId().toString()),
                    new StorageItem("experience", ePlayer.getExperience()),
                    new StorageItem("mobKills", ePlayer.getMobKills()),
                    new StorageItem("playerKills", ePlayer.getPlayerKills()),
                    new StorageItem("deaths", ePlayer.getDeaths()),
                    new StorageItem("killstreak", ePlayer.getKillstreak()),
                    new StorageItem("bestKillstreak", ePlayer.getBestKillstreak()));
        }

        for (UUID uuid : instance.getBoostManager().getBoosts().keySet()) {
            Boost boost = instance.getBoostManager().getBoost(uuid);
            prepareSaveItem("boosts", new StorageItem("uuid", uuid.toString()),
                    new StorageItem("expiration", boost.getExpiration()),
                    new StorageItem("multiplier", boost.getMultiplier()));
        }

        if (instance.getBoostManager().getGlobalBoost() != null) {
            Boost boost = instance.getBoostManager().getGlobalBoost();
            prepareSaveItem("boosts", new StorageItem("global", true),
                    new StorageItem("expiration", boost.getExpiration()),
                    new StorageItem("multiplier", boost.getMultiplier()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}
