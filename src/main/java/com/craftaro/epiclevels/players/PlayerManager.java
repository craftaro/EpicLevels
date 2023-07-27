package com.craftaro.epiclevels.players;

import com.craftaro.epiclevels.EpicLevels;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, EPlayer> registeredPlayers = new HashMap<>();

    private final EpicLevels plugin;
    private long lastUpdate = -1;

    public PlayerManager(EpicLevels plugin) {
        this.plugin = plugin;
    }

    public EPlayer getPlayer(UUID uuid) {
        return this.registeredPlayers.computeIfAbsent(uuid, u -> {
            this.plugin.getDataHelper().getPlayerOrCreate(uuid, this::addPlayer);
            return new EPlayer(uuid);
        });
    }

    public EPlayer getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public void addPlayer(EPlayer player) {
        final EPlayer registered;
        if ((registered = registeredPlayers.get(player.getUniqueId())) != null && registered.getExperience() > player.getExperience()) {
            return;
        }
        this.registeredPlayers.put(player.getUniqueId(), player);

        this.lastUpdate = System.currentTimeMillis();
    }

    public void addPlayers(Map<UUID, EPlayer> players) {
        this.registeredPlayers.putAll(players);

        this.lastUpdate = System.currentTimeMillis();
    }

    public List<EPlayer> getPlayers() {
        List<EPlayer> result = new ArrayList<>(this.registeredPlayers.values());
        result.sort(Comparator.comparingDouble(EPlayer::getExperience).reversed());

        return result;
    }

    public List<EPlayer> getPlayersUnsorted() {
        return new ArrayList<>(this.registeredPlayers.values());
    }

    public EPlayer resetPlayer(UUID uuid) {
        this.lastUpdate = System.currentTimeMillis();

        return this.registeredPlayers.remove(uuid);
    }

    // TODO: Probably cache sorted versions of the list instead, for better result
    public long getLastUpdate() {
        return this.lastUpdate;
    }

    public boolean containsPlayer(UUID uuid) {
        return this.registeredPlayers.containsKey(uuid);
    }
}
