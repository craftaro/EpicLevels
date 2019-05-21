package com.songoda.epiclevels.players;

import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {

    private static final Map<UUID, EPlayer> registeredPlayers = new HashMap<>();

    public EPlayer getPlayer(UUID uuid) {
        return registeredPlayers.computeIfAbsent(uuid, u -> new EPlayer(uuid));
    }

    public EPlayer getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public EPlayer addPlayer(EPlayer player) {
        registeredPlayers.put(player.getUniqueId(), player);
        return player;
    }

    public List<EPlayer> getPlayers() {
        List<EPlayer> players = registeredPlayers.values().stream().sorted(Comparator.comparingLong(EPlayer::getExperience)).collect(Collectors.toList());
        Collections.reverse(players);
        return players;
    }

    public void resetPlayer(UUID uuid) {
        registeredPlayers.remove(uuid);
    }

}
