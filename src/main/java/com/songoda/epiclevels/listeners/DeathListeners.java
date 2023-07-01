package com.songoda.epiclevels.listeners;

import com.craftaro.core.utils.TextUtils;
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
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        EPlayer damaged = this.plugin.getPlayerManager().getPlayer(event.getEntity().getUniqueId()); // A very sensitive person with a troubling past.
        EPlayer damager = this.plugin.getPlayerManager().getPlayer(event.getDamager().getUniqueId()); // The douche who ruined the guys life.

        if (Settings.BLACKLISTED_WORLDS.getStringList().stream()
                .anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName()))) {
            return;
        }

        if (damager.getLevel() < Settings.START_PVP_LEVEL.getInt()) {
            this.plugin.getLocale().getMessage("event.pvp.deny").sendPrefixedMessage(damager.getPlayer().getPlayer());
            event.setCancelled(true);
        } else if (damaged.getLevel() < Settings.START_PVP_LEVEL.getInt()) {
            this.plugin.getLocale().getMessage("event.pvp.denythem").sendPrefixedMessage(damager.getPlayer().getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        if (Settings.BLACKLISTED_WORLDS.getStringList().stream().anyMatch(worldStr -> worldStr.equalsIgnoreCase(event.getEntity().getWorld().getName()))) {
            return;
        }

        double expPlayer = Settings.EXP_PLAYER.getDouble();

        Player killer = event.getEntity().getKiller();
        EPlayer eKiller = this.plugin.getPlayerManager().getPlayer(killer);

        if (event.getEntity() instanceof Player) {
            Player killed = (Player) event.getEntity();
            EPlayer eKilled = this.plugin.getPlayerManager().getPlayer(killed);

            if (killer == killed) {
                return;
            }

            if (!eKiller.canGainExperience(killed.getUniqueId()) && Settings.ANTI_GRINDER.getBoolean()) {
                if (Settings.GRINDER_ALERT.getBoolean()) {
                    this.plugin.getLocale().getMessage("event.antigrinder.trigger")
                            .processPlaceholder("killed", killed.getPlayer().getName())
                            .sendPrefixedMessage(killer);
                }
                return;
            }

            eKilled.addDeath();

            double killedExpBefore = eKilled.getExperience();
            double killedExpAfter = eKilled.addExperience(0L - Settings.EXP_DEATH.getDouble());

            if (Settings.SEND_DEATH_MESSAGE.getBoolean()) {
                this.plugin.getLocale().getMessage("event.player.death")
                        .processPlaceholder("name", ChatColor.stripColor(killer.getName()))
                        .processPlaceholder("hearts", (int) Math.floor(killer.getHealth()))
                        .processPlaceholder("exp", Methods.formatDecimal(-(killedExpAfter - killedExpBefore)))
                        .sendPrefixedMessage(killed);
            }

            if (Settings.SEND_BROADCAST_DEATH_MESSAGE.getBoolean()) {
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != killer && p != killed).collect(Collectors.toList())) {
                    this.plugin.getLocale().getMessage("event.player.death.broadcast")
                            .processPlaceholder("killer", killer.getName())
                            .processPlaceholder("killed", killed.getName())
                            .processPlaceholder("hearts", (int) Math.floor(killer.getHealth()))
                            .processPlaceholder("exp", killer.getName())
                            .sendPrefixedMessage(pl);
                }
            }

            if (Settings.SEND_KILLSTREAK_BROKEN_MESSAGE.getBoolean() && eKilled.getKillStreak() >= Settings.SEND_KILLSTREAK_ALERTS_AFTER.getInt()) {
                this.plugin.getLocale().getMessage("event.killstreak.broken")
                        .processPlaceholder("streak", eKilled.getKillStreak())
                        .sendPrefixedMessage(killed);
                this.plugin.getLocale().getMessage("event.killstreak.broke")
                        .processPlaceholder("killed", killed.getName())
                        .processPlaceholder("streak", eKilled.getKillStreak())
                        .sendPrefixedMessage(killer);
            }

            if (Settings.SEND_BROADCAST_BROKEN_KILLSTREAK.getBoolean() && eKilled.getKillStreak() >= Settings.SEND_KILLSTREAK_ALERTS_AFTER.getInt()) {
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != killer && p != killed).collect(Collectors.toList())) {
                    this.plugin.getLocale().getMessage("event.killstreak.brokenannounce")
                            .processPlaceholder("player", killer.getName())
                            .processPlaceholder("killed", killed.getName())
                            .processPlaceholder("streak", eKilled.getKillStreak())
                            .sendPrefixedMessage(pl);
                }
            }

            eKilled.resetKillStreak();

            eKiller.increaseKillStreak();
            eKiller.addPlayerKill(killed.getUniqueId());

            this.plugin.getDataManager().updatePlayer(eKiller);
            this.plugin.getDataManager().updatePlayer(eKilled);

            double playerExpBefore = eKiller.getExperience();
            double playerExpAfter = eKiller.addExperience(expPlayer);

            if (Settings.SEND_PLAYER_KILL_MESSAGE.getBoolean()) {
                this.plugin.getLocale().getMessage("event.player.killed")
                        .processPlaceholder("name", ChatColor.stripColor(killed.getDisplayName()))
                        .processPlaceholder("hearts", (int) Math.floor(killer.getHealth()))
                        .processPlaceholder("exp", Methods.formatDecimal(playerExpAfter - playerExpBefore))
                        .sendPrefixedMessage(killer);
            }

            int every = Settings.RUN_KILLSTREAK_EVERY.getInt();
            if (every != 0 && eKiller.getKillStreak() % every == 0) {
                Killstreak def = this.plugin.getKillstreakManager().getKillStreak(-1);
                if (def != null) {
                    Rewards.run(def.getRewards(), killer, eKiller.getKillStreak(), true);
                }
                if (this.plugin.getKillstreakManager().getKillStreak(eKiller.getKillStreak()) == null) {
                    return;
                }
                Rewards.run(this.plugin.getKillstreakManager().getKillStreak(eKiller.getKillStreak()).getRewards(), killer, eKiller.getKillStreak(), true);
            }
        } else {
            eKiller.addMobKill();

            double mobExperience = this.plugin.getEntityManager().getExperience(event.getEntityType());
            double playerExpBefore = eKiller.getExperience();
            double playerExpAfter = eKiller.addExperience(mobExperience);

            this.plugin.getDataManager().updatePlayer(eKiller);

            if (Settings.SEND_MOB_KILL_MESSAGE.getBoolean()) {
                this.plugin.getLocale().getMessage("event.mob.killed")
                        .processPlaceholder("type", TextUtils.formatText(event.getEntity().getType().name(), true))
                        .processPlaceholder("exp", Methods.formatDecimal(playerExpAfter - playerExpBefore))
                        .sendPrefixedMessage(killer);
            }
        }
    }
}
