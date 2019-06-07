package com.songoda.epiclevels.players;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.levels.Level;
import com.songoda.epiclevels.utils.Rewards;
import com.songoda.epiclevels.utils.settings.Setting;
import com.songoda.epiclevels.utils.settings.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EPlayer {

    private final UUID uuid;

    private double experience;

    private int mobKills;
    private int playerKills;
    private int deaths;
    private int killstreak;
    private int bestKillstreak;

    private Map<Long, UUID> kills = new HashMap<>();

    private static ScriptEngine engine = null;

    public EPlayer(UUID uuid, double experience, int mobKills, int playerKills, int deaths, int killstreak, int bestKillstreak) {
        this.uuid = uuid;
        this.experience = experience;
        this.mobKills = mobKills;
        this.playerKills = playerKills;
        this.deaths = deaths;
        this.killstreak = killstreak;
        this.bestKillstreak = bestKillstreak;

        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
        }
    }

    public EPlayer(UUID uuid) {
        this(uuid, Setting.START_EXP.getDouble(), 0, 0, 0, 0, 0);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public double addExperience(double experience) {
        if (experience == 0) return experience;
        EpicLevels plugin = EpicLevels.getInstance();
        if (experience < 0L) {
            if (this.experience + experience < 0L && !Setting.ALLOW_NEGATIVE.getBoolean())
                this.experience = 0L;
            else
                this.experience = this.experience + experience;

            return this.experience;
        }
        int currentLevel = getLevel();

        Boost boost = plugin.getBoostManager().getBoost(uuid);
        double boostMultiplier = boost == null ? 1 : boost.getMultiplier();

        this.experience += ((this.experience + experience < 0 ? 0 : experience) * multiplier()) * boostMultiplier;

        double bonus = Setting.KILLSTREAK_BONUS_EXP.getDouble();
        this.experience += bonus * killstreak;

        Player player = getPlayer().getPlayer();
        if ((currentLevel != getLevel() || currentLevel > getLevel()) && player != null) {
            for (int i = currentLevel; i <= getLevel() ; i++) {
                Level def = plugin.getLevelManager().getLevel(-1);
                if (def != null)
                    Rewards.run(def.getRewards(), player, i, i == getLevel());
                if (plugin.getLevelManager().getLevel(i) == null) continue;
                Rewards.run(plugin.getLevelManager().getLevel(i).getRewards(), player, i, i == getLevel());
            }

            if (Setting.SEND_BROADCAST_LEVELUP_MESSAGE.getBoolean()
                    && getLevel() % Setting.BROADCAST_LEVELUP_EVERY.getInt() == 0)
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != player).collect(Collectors.toList()))
                    pl.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.levelup.announcement", player.getName(), getLevel()));
        }
        if (this.experience > Setting.MAX_EXP.getDouble())
            this.experience = Setting.MAX_EXP.getDouble();
        return this.experience;
    }

    public boolean canGainExperience(UUID uuid) {
        int triggerAmount = Setting.GRINDER_MAX.getInt();
        int maxInterval = Setting.GRINDER_INTERVAL.getInt() * 1000;
        return kills.keySet().stream().filter(x -> kills.get(x).equals(uuid))
                .filter(x -> System.currentTimeMillis() - x < maxInterval).count() < triggerAmount;
    }

    private int multiplier() {
        int multiplier = 1;
        if (!getPlayer().isOnline()) return multiplier;
        for (PermissionAttachmentInfo permissionAttachmentInfo : getPlayer().getPlayer().getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epiclevels.multiplier")) continue;
            multiplier = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
        }
        return multiplier;
    }

    public double getExperience() {
        return experience;
    }

    public int getKills() {
        return mobKills + playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public int addMobKill() {
        return mobKills++;
    }

    public int addPlayerKill(UUID uuid) {
        this.kills.put(System.currentTimeMillis(), uuid);
        return playerKills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public int addDeath() {
        return deaths++;
    }

    public int getKillstreak() {
        return killstreak;
    }

    public int getBestKillstreak() {
        return bestKillstreak;
    }

    public int increaseKillstreak() {
        killstreak++;
        if (killstreak > bestKillstreak)
            bestKillstreak = killstreak;
        return killstreak;
    }

    public void resetKillstreak() {
        killstreak = 0;
    }

    public int getLevel() {
        int lastLevel = 0;
        for (int i = 1; i <= Setting.MAX_LEVEL.getInt(); i++) {
            if (experience(i) > experience) break;
            lastLevel++;
        }
        return lastLevel;
    }

    public static int experience(int level) {
        Formula formula = Formula.valueOf(Setting.LEVELING_FORMULA.getString());
        switch (formula) {
            case EXPONENTIAL: {
                double a = 0;
                for (int i = 1; i < level; i++)
                    a += Math.floor(i + Setting.EXPONENTIAL_BASE.getDouble()
                            * Math.pow(2, (i / Setting.EXPONENTIAL_DIVISOR.getDouble())));
                return (int) Math.floor(a);
            }
            case LINEAR: {
                int a = 0;
                for (int i = 1; i < level; i++)
                    a += Setting.LINEAR_INCREMENT.getInt();
                return a;
            }
            case CUSTOM: {
                try {
                    return Integer.parseInt(engine.eval(Setting.CUSTOM_FORMULA.getString()
                            .replace("level", String.valueOf(level))).toString());
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        return 0;
    }

    public enum Formula {
        LINEAR, EXPONENTIAL, CUSTOM
    }
}
