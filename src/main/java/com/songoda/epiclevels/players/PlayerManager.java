package com.songoda.epiclevels.players;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {

    private final Map<UUID, EPlayer> registeredPlayers = new HashMap<>();

    public EPlayer getPlayer(UUID uuid) {
        return registeredPlayers.computeIfAbsent(uuid, u -> {
            EPlayer ePlayer = new EPlayer(uuid);
            EpicLevels.getInstance().getDataManager().createPlayer(ePlayer);
            return ePlayer;
        });
    }

    public EPlayer getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public void addPlayer(EPlayer player) {
        registeredPlayers.put(player.getUniqueId(), player);
    }

    public void addPlayers(Map<UUID, EPlayer> players) {
        registeredPlayers.putAll(players);
    }

    public List<EPlayer> getPlayers() {
        List<EPlayer> players = registeredPlayers.values().stream().sorted(Comparator.comparingDouble(EPlayer::getExperience)).collect(Collectors.toList());
        Collections.reverse(players);
        return players;
    }

    public EPlayer resetPlayer(UUID uuid) {
        return registeredPlayers.remove(uuid);
    }
}
