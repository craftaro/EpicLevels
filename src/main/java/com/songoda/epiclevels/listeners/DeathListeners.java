package com.songoda.epiclevels.listeners;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.killstreaks.Killstreak;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.Rewards;
import com.songoda.epiclevels.utils.settings.Setting;
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
        EPlayer damager = plugin.getPlayerManager().getPlayer(event.getDamager().getUniqueId()); // The douche who ruined the guys life.

        if (Setting.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        if (damager.getLevel() < Setting.START_PVP_LEVEL.getInt()) {
            damager.getPlayer().getPlayer().sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.pvp.deny"));
            event.setCancelled(true);
        } else if (damaged.getLevel() < Setting.START_PVP_LEVEL.getInt()) {
            damager.getPlayer().getPlayer().sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.pvp.denythem"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;

        if (Setting.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        double expPlayer = Setting.EXP_PLAYER.getDouble();
        double expMob = Setting.EXP_MOB.getDouble();

        Player player = event.getEntity().getKiller();
        EPlayer ePlayer = plugin.getPlayerManager().getPlayer(player);

        if (event.getEntity() instanceof Player) {
            Player killed = (Player) event.getEntity();
            EPlayer eKilled = plugin.getPlayerManager().getPlayer(killed);

            eKilled.addDeath();
            eKilled.resetKillstreak();

            double killedExpBefore = eKilled.getExperience();
            double killedExpAfter = eKilled.addExperience(0L - Setting.EXP_DEATH.getDouble());

            if (Setting.SEND_DEATH_MESSAGE.getBoolean())
                killed.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.death", ChatColor.stripColor(player.getName()), -(killedExpAfter - killedExpBefore)));

            if (Setting.SEND_BROADCAST_DEATH_MESSAGE.getBoolean())
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != player && p != killed).collect(Collectors.toList()))
                    pl.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.death.broadcast", killed.getName(), player.getName()));

            if (!ePlayer.canGainExperience(killed.getUniqueId()) && Setting.ANTI_GRINDER.getBoolean()) {
                if (Setting.GRINDER_ALERT.getBoolean())
                    player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.antigrinder.trigger", killed.getPlayer().getName()));
                return;
            }

            ePlayer.increaseKillstreak();
            ePlayer.addPlayerKill(killed.getUniqueId());

            int every = Setting.RUN_KILLSTREAK_EVERY.getInt();
            if (every != 0 && ePlayer.getKillstreak() % every == 0) {
                Killstreak def = plugin.getKillstreakManager().getKillstreak(-1);
                if (def != null)
                    Rewards.run(def.getRewards(), player, ePlayer.getKillstreak(), true);
                if (plugin.getKillstreakManager().getKillstreak(ePlayer.getKillstreak()) == null) return;
                Rewards.run(plugin.getKillstreakManager().getKillstreak(ePlayer.getKillstreak()).getRewards(), player, ePlayer.getKillstreak(), true);
            }

            double playerExpBefore = ePlayer.getExperience();
            double playerExpAfter = ePlayer.addExperience(expPlayer);

            if (Setting.SEND_PLAYER_KILL_MESSAGE.getBoolean())
                player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.player.killed", ChatColor.stripColor(killed.getDisplayName()), playerExpAfter - playerExpBefore));

        } else {
            ePlayer.addMobKill();
            double playerExpBefore = ePlayer.getExperience();
            double playerExpAfter = ePlayer.addExperience(expMob);

            if (Setting.SEND_MOB_KILL_MESSAGE.getBoolean())
                player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.mob.killed", event.getEntity().getName(), playerExpAfter - playerExpBefore));
        }

    }
}
