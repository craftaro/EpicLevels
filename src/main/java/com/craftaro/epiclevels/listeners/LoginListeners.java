package com.craftaro.epiclevels.listeners;

import com.craftaro.epiclevels.EpicLevels;
import com.craftaro.epiclevels.players.EPlayer;
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
                () -> this.plugin.getDataHelper().getPlayerOrCreate(event.getPlayer(), player -> {
                    player.setSaved(false);
                    this.plugin.getPlayerManager().addPlayer(player);
                }),
                40);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        EPlayer ePlayer = this.plugin.getPlayerManager().getPlayer(event.getPlayer());
        this.plugin.getDataHelper().updatePlayer(ePlayer);
        ePlayer.setSaved(true);
    }
}
