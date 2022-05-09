package com.songoda.epiclevels.players;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, EPlayer> registeredPlayers = new HashMap<>();

    private long lastUpdate = -1;

    public EPlayer getPlayer(UUID uuid) {
        return registeredPlayers.computeIfAbsent(uuid, u -> {
            EpicLevels.getInstance().getDataManager().getPlayerOrCreate(uuid, this::addPlayer);
            return new EPlayer(uuid);
        });
    }

    public EPlayer getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public void addPlayer(EPlayer player) {
        registeredPlayers.put(player.getUniqueId(), player);

        this.lastUpdate = System.currentTimeMillis();
    }

    public void addPlayers(Map<UUID, EPlayer> players) {
        registeredPlayers.putAll(players);

        this.lastUpdate = System.currentTimeMillis();
    }

    public List<EPlayer> getPlayers() {
        List<EPlayer> result = new ArrayList<>(registeredPlayers.values());
        result.sort(Comparator.comparingDouble(EPlayer::getExperience).reversed());

        return result;
    }

    public List<EPlayer> getPlayersUnsorted() {
        return new ArrayList<>(registeredPlayers.values());
    }

    public EPlayer resetPlayer(UUID uuid) {
        this.lastUpdate = System.currentTimeMillis();

        return registeredPlayers.remove(uuid);
    }

    // TODO: Probably cache sorted versions of the list instead, for better result
    public long getLastUpdate() {
        return this.lastUpdate;
    }

    public boolean containsPlayer(UUID uuid) {
        return registeredPlayers.containsKey(uuid);
    }
}
