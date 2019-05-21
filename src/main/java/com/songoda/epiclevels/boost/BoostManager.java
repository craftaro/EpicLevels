package com.songoda.epiclevels.boost;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoostManager {

    private final Map<UUID, Boost> registeredBoosts = new HashMap<>();

    private Boost globalBoost = null;

    public Map<UUID, Boost> getBoosts() {
        return Collections.unmodifiableMap(registeredBoosts);
    }

    public Boost getBoost(UUID uuid) {
        EpicLevels epicLevels = EpicLevels.getInstance();
        if (globalBoost != null) {
            if (globalBoost.getExpiration() > System.currentTimeMillis())
                return globalBoost;
            else {
                globalBoost = null;
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendMessage(epicLevels.getReferences().getPrefix() + epicLevels.getLocale().getMessage("event.boost.globalexpire")));
            }
        }
        Boost boost = registeredBoosts.get(uuid);
        if (boost == null) return null;
        if (boost.getExpiration() > System.currentTimeMillis())
            return boost;
        else {
            registeredBoosts.remove(uuid);
            Bukkit.getPlayer(uuid).sendMessage(epicLevels.getReferences().getPrefix() + epicLevels.getLocale().getMessage("event.boost.expire"));
        }
        return null;
    }

    public Boost addBoost(UUID uuid, Boost boost) {
        removeBoost(uuid);
        return registeredBoosts.put(uuid, boost);
    }

    public Boost removeBoost(UUID uuid) {
        if (registeredBoosts.get(uuid) == null) return null;

        Boost boost = registeredBoosts.get(uuid);
        boost.expire();
        return boost;
    }

    public Boost getGlobalBoost() {
        return globalBoost;
    }

    public Boost setGlobalBoost(Boost boost) {
        this.globalBoost = boost;
        return boost;
    }

    public void clearGlobalBoost() {
        if (globalBoost == null) return;
        globalBoost.expire();
    }
}
