package com.songoda.epiclevels.boost;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoostManager {
    private final Map<UUID, Boost> registeredBoosts = new HashMap<>();

    private final EpicLevels plugin;

    private Boost globalBoost = null;

    public BoostManager(EpicLevels plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, Boost> getBoosts() {
        return Collections.unmodifiableMap(this.registeredBoosts);
    }

    public Boost getBoost(UUID uuid) {
        if (this.globalBoost != null) {
            if (this.globalBoost.getExpiration() > System.currentTimeMillis()) {
                return this.globalBoost;
            } else {
                this.plugin.getDataHelper().deleteBoost(this.globalBoost);
                this.globalBoost = null;
                Bukkit.getOnlinePlayers().forEach(player -> this.plugin.getLocale().getMessage("event.boost.globalexpire").sendPrefixedMessage(player));
            }
        }
        Boost boost = this.registeredBoosts.get(uuid);
        if (boost == null) {
            return null;
        }
        if (boost.getExpiration() > System.currentTimeMillis()) {
            return boost;
        } else {
            this.plugin.getDataHelper().deleteBoost(boost);
            this.registeredBoosts.remove(uuid);
            this.plugin.getLocale().getMessage("event.boost.expire").sendPrefixedMessage(Bukkit.getPlayer(uuid));
        }
        return null;
    }

    public Boost addBoost(UUID uuid, Boost boost) {
        removeBoost(uuid);
        return this.registeredBoosts.put(uuid, boost);
    }

    public void addBoosts(Map<UUID, Boost> uuidBoostMap) {
        for (Map.Entry<UUID, Boost> entry : uuidBoostMap.entrySet()) {
            if (entry.getKey() == null) {
                this.globalBoost = entry.getValue();
            } else {
                this.registeredBoosts.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Boost removeBoost(UUID uuid) {
        if (this.registeredBoosts.get(uuid) == null) {
            return null;
        }

        Boost boost = this.registeredBoosts.get(uuid);
        boost.expire();
        return boost;
    }

    public Boost getGlobalBoost() {
        return this.globalBoost;
    }

    public Boost setGlobalBoost(Boost boost) {
        this.globalBoost = boost;
        return boost;
    }

    public Boost clearGlobalBoost() {
        if (this.globalBoost == null) {
            return null;
        }

        this.globalBoost.expire();
        return this.globalBoost;
    }
}
