package com.songoda.epiclevels.listeners;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.killstreaks.Killstreak;
import com.songoda.epiclevels.levels.Level;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.Rewards;
import com.songoda.epiclevels.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.stream.Collectors;

public class DeathListeners implements Listener {

    private final EpicLevels plugin;

    public DeathListeners(EpicLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        EPlayer damaged = plugin.getPlayerManager().getPlayer(event.getEntity().getUniqueId()); // A very sensitive person with a troubling past.
        EPlayer damager = plugin.getPlayerManager().getPlayer(event.getDamager().getUniqueId());

        if (SettingsManager.Setting.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        if (damager.getLevel() < SettingsManager.Setting.START_PVP_LEVEL.getInt()) {
            damager.getPlayer().getPlayer().sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage(" event.pvp.deny"));
            return;
        } else if (damaged.getLevel() < SettingsManager.Setting.START_PVP_LEVEL.getInt()) {
            damager.getPlayer().getPlayer().sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage(" event.pvp.denythem"));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;

        if (SettingsManager.Setting.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        long expPlayer = SettingsManager.Setting.EXP_PLAYER.getLong();
        long expMob = SettingsManager.Setting.EXP_MOB.getLong();

        Player player = event.getEntity().getKiller();
        EPlayer ePlayer = plugin.getPlayerManager().getPlayer(player);

        if (event.getEntity() instanceof Player) {
            if (expPlayer == 0) return;
            Player killed = (Player) event.getEntity();
            EPlayer eKilled = plugin.getPlayerManager().getPlayer(killed);

            eKilled.addDeath();
            eKilled.resetKillstreak();

            long killedExpBefore = eKilled.getExperience();
            long killedExpAfter = eKilled.addExperience(0L - SettingsManager.Setting.EXP_DEATH.getLong());

            if (SettingsManager.Setting.SEND_DEATH_MESSAGE.getBoolean())
                killed.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.death", ChatColor.stripColor(player.getDisplayName()), -(killedExpAfter - killedExpBefore)));

            if (SettingsManager.Setting.SEND_BROADCAST_DEATH_MESSAGE.getBoolean())
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != player && p != killed).collect(Collectors.toList()))
                    pl.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.death.broadcast", killed.getName(), player.getName()));

            if (!ePlayer.canGainExperience(killed.getUniqueId()) && SettingsManager.Setting.ANTI_GRINDER.getBoolean()) {
                if (SettingsManager.Setting.GRINDER_ALERT.getBoolean())
                    player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.antigrinder.trigger", killed.getPlayer().getName()));
                return;
            }

            ePlayer.increaseKillstreak();
            ePlayer.addPlayerKill(killed.getUniqueId());

            int every = SettingsManager.Setting.RUN_KILLSTREAK_EVERY.getInt();
            if (every != 0 && ePlayer.getKillstreak() % every == 0) {
                Killstreak def = plugin.getKillstreakManager().getKillstreak(-1);
                if (def != null)
                    Rewards.run(def.getRewards(), player, ePlayer.getKillstreak(), true);
                if (plugin.getKillstreakManager().getKillstreak(ePlayer.getKillstreak()) == null) return;
                Rewards.run(plugin.getKillstreakManager().getKillstreak(ePlayer.getKillstreak()).getRewards(), player, ePlayer.getKillstreak(), true);
            }

            long playerExpBefore = ePlayer.getExperience();
            long playerExpAfter = ePlayer.addExperience(expPlayer);

            if (SettingsManager.Setting.SEND_KILL_MESSAGE.getBoolean())
                player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.killed", ChatColor.stripColor(killed.getDisplayName()), playerExpAfter - playerExpBefore));

        } else {
            if (expMob == 0) return;
            ePlayer.addMobKill();
            long playerExpBefore = ePlayer.getExperience();
            long playerExpAfter = ePlayer.addExperience(expMob);
            player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.mob.killed", event.getEntity().getName(), playerExpAfter - playerExpBefore));
        }

    }
}
