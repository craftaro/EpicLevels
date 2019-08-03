package com.songoda.epiclevels.listeners;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListeners implements Listener {

    private final EpicLevels plugin;

    public LoginListeners(EpicLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getDataManager().getPlayer(event.getPlayer(), (ePlayer ->
                plugin.getPlayerManager().addPlayer(ePlayer)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        EPlayer ePlayer = plugin.getPlayerManager().getPlayer(event.getPlayer());
        plugin.getDataManager().updatePlayer(ePlayer);
    }
}
