package com.songoda.epiclevels.listeners;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import org.bukkit.Bukkit;
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
        Bukkit.getScheduler().runTaskLater(this.plugin,
                () -> this.plugin.getDataHelper().getPlayerOrCreate(event.getPlayer(), player -> this.plugin.getPlayerManager().addPlayer(player)),
                30);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        EPlayer ePlayer = this.plugin.getPlayerManager().getPlayer(event.getPlayer());
        this.plugin.getDataHelper().updatePlayer(ePlayer);
    }
}
