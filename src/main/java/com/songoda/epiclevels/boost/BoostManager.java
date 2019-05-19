package com.songoda.epiclevels.boost;

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
        if (globalBoost != null) {
            if (globalBoost.getExpiration() > System.currentTimeMillis())
                return globalBoost;
            else
                globalBoost = null;
        }
        Boost boost = registeredBoosts.get(uuid);
        if (boost == null) return null;
        if (boost.getExpiration() > System.currentTimeMillis())
            return boost;
        else
            removeBoost(uuid);
        return null;
    }

    public Boost addBoost(UUID uuid, Boost boost) {
        removeBoost(uuid);
        return registeredBoosts.put(uuid, boost);
    }

    public Boost removeBoost(UUID uuid) {
        return registeredBoosts.remove(uuid);
    }

    public Boost getGlobalBoost() {
        return globalBoost;
    }

    public Boost setGlobalBoost(Boost boost) {
        this.globalBoost = boost;
        return boost;
    }

    public void clearGlobalBoost() {
        this.globalBoost = null;
    }
}
