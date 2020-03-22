package com.songoda.epiclevels.listeners;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.killstreaks.Killstreak;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.settings.Settings;
import com.songoda.epiclevels.utils.Methods;
import com.songoda.epiclevels.utils.Rewards;
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


        if (Settings.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        if (damager.getLevel() < Settings.START_PVP_LEVEL.getInt()) {
            plugin.getLocale().getMessage("event.pvp.deny").sendPrefixedMessage(damager.getPlayer().getPlayer());
            event.setCancelled(true);
        } else if (damaged.getLevel() < Settings.START_PVP_LEVEL.getInt()) {
            plugin.getLocale().getMessage("event.pvp.denythem").sendPrefixedMessage(damager.getPlayer().getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;

        if (Settings.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName())))
            return;

        double expPlayer = Settings.EXP_PLAYER.getDouble();
        double expMob = Settings.EXP_MOB.getDouble();

        Player killer = event.getEntity().getKiller();
        EPlayer eKiller = plugin.getPlayerManager().getPlayer(killer);

        if (event.getEntity() instanceof Player) {
            Player killed = (Player) event.getEntity();
            EPlayer eKilled = plugin.getPlayerManager().getPlayer(killed);

            if (killer == killed) return;

            if (!eKiller.canGainExperience(killed.getUniqueId()) && Settings.ANTI_GRINDER.getBoolean()) {
                if (Settings.GRINDER_ALERT.getBoolean())
                    plugin.getLocale().getMessage("event.antigrinder.trigger")
                            .processPlaceholder("killed", killed.getPlayer().getName())
                            .sendPrefixedMessage(killer);
                return;
            }

            eKilled.addDeath();

            double killedExpBefore = eKilled.getExperience();
            double killedExpAfter = eKilled.addExperience(0L - Settings.EXP_DEATH.getDouble());

            if (Settings.SEND_DEATH_MESSAGE.getBoolean())
                plugin.getLocale().getMessage("event.player.death")
                        .processPlaceholder("name", ChatColor.stripColor(killer.getName()))
                        .processPlaceholder("hearts", Methods.formatDecimal(killer.getHealth()))
                        .processPlaceholder("exp", Methods.formatDecimal(-(killedExpAfter - killedExpBefore))).sendPrefixedMessage(killed);

            if (Settings.SEND_BROADCAST_DEATH_MESSAGE.getBoolean())
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != killer && p != killed).collect(Collectors.toList()))
                    plugin.getLocale().getMessage("event.player.death.broadcast")
                            .processPlaceholder("killer", killer.getName())
                            .processPlaceholder("killed", killed.getName())
                            .processPlaceholder("hearts", Methods.formatDecimal(killer.getHealth()))
                            .processPlaceholder("exp", killer.getName())
                            .sendPrefixedMessage(pl);

            if (Settings.SEND_KILLSTREAK_BROKEN_MESSAGE.getBoolean() && eKilled.getKillstreak() >= Settings.SEND_KILLSTREAK_ALERTS_AFTER.getInt()) {
                plugin.getLocale().getMessage("event.killstreak.broken")
                        .processPlaceholder("streak", eKilled.getKillstreak())
                        .sendPrefixedMessage(killed);
                plugin.getLocale().getMessage("event.killstreak.broke")
                        .processPlaceholder("killed", killed.getName())
                        .processPlaceholder("streak", eKilled.getKillstreak())
                        .sendPrefixedMessage(killer);
            }

            if (Settings.SEND_BROADCAST_BROKEN_KILLSTREAK.getBoolean() && eKilled.getKillstreak() >= Settings.SEND_KILLSTREAK_ALERTS_AFTER.getInt())
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != killer && p != killed).collect(Collectors.toList()))
                    plugin.getLocale().getMessage("event.killstreak.brokenannounce")
                            .processPlaceholder("player", killer.getName())
                            .processPlaceholder("killed", killed.getName())
                            .processPlaceholder("streak", eKilled.getKillstreak())
                            .sendPrefixedMessage(pl);

            eKilled.resetKillstreak();

            eKiller.increaseKillstreak();
            eKiller.addPlayerKill(killed.getUniqueId());

            plugin.getDataManager().updatePlayer(eKiller);
            plugin.getDataManager().updatePlayer(eKilled);

            int every = Settings.RUN_KILLSTREAK_EVERY.getInt();
            if (every != 0 && eKiller.getKillstreak() % every == 0) {
                Killstreak def = plugin.getKillstreakManager().getKillstreak(-1);
                if (def != null)
                    Rewards.run(def.getRewards(), killer, eKiller.getKillstreak(), true);
                if (plugin.getKillstreakManager().getKillstreak(eKiller.getKillstreak()) == null) return;
                Rewards.run(plugin.getKillstreakManager().getKillstreak(eKiller.getKillstreak()).getRewards(), killer, eKiller.getKillstreak(), true);
            }

            double playerExpBefore = eKiller.getExperience();
            double playerExpAfter = eKiller.addExperience(expPlayer);

            if (Settings.SEND_PLAYER_KILL_MESSAGE.getBoolean())
                plugin.getLocale().getMessage("event.player.killed")
                        .processPlaceholder("name", ChatColor.stripColor(killed.getDisplayName()))
                        .processPlaceholder("hearts", Methods.formatDecimal(killer.getHealth()))
                        .processPlaceholder("exp", Methods.formatDecimal(playerExpAfter - playerExpBefore))
                        .sendPrefixedMessage(killer);

        } else {
            eKiller.addMobKill();
            double playerExpBefore = eKiller.getExperience();
            double playerExpAfter = eKiller.addExperience(expMob);

            plugin.getDataManager().updatePlayer(eKiller);

            if (Settings.SEND_MOB_KILL_MESSAGE.getBoolean()) {
                plugin.getLocale().getMessage("event.mob.killed")
                        .processPlaceholder("type", Methods.formatText(event.getEntity().getType().name(), true))
                        .processPlaceholder("exp", Methods.formatDecimal(playerExpAfter - playerExpBefore))
                        .sendPrefixedMessage(killer);
            }
        }

    }
}
