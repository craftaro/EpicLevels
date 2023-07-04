package com.songoda.epiclevels.players;

import com.songoda.core.math.MathUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.levels.Level;
import com.songoda.epiclevels.settings.Settings;
import com.songoda.epiclevels.utils.Rewards;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class EPlayer {
    private final UUID uuid;

    private double experience;

    private int mobKills;
    private int playerKills;
    private int deaths;
    private int killStreak;
    private int bestKillStreak;

    private final Map<Long, UUID> kills = new HashMap<>();
    // No serialized variable
    private transient boolean saved = true;

    public EPlayer(UUID uuid, double experience, int mobKills, int playerKills, int deaths, int killStreak, int bestKillStreak) {
        this.uuid = uuid;
        this.experience = experience;
        this.mobKills = mobKills;
        this.playerKills = playerKills;
        this.deaths = deaths;
        this.killStreak = killStreak;
        this.bestKillStreak = bestKillStreak;
    }

    public EPlayer(UUID uuid) {
        this(uuid, Settings.START_EXP.getDouble(), 0, 0, 0, 0, 0);
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.uuid);
    }

    public double addExperience(double experience) {
        if (experience == 0) {
            return this.experience;
        }

        this.saved = false;
        EpicLevels plugin = EpicLevels.getPlugin(EpicLevels.class);
        if (experience < 0L) {
            if (this.experience + experience < 0L && !Settings.ALLOW_NEGATIVE.getBoolean()) {
                this.experience = 0L;
            } else {
                this.experience = this.experience + experience;
            }

            return this.experience;
        }
        int currentLevel = getLevel();

        Boost boost = plugin.getBoostManager().getBoost(this.uuid);
        double boostMultiplier = boost == null ? 1 : boost.getMultiplier();

        this.experience += ((this.experience + experience < 0 ? 0 : experience) * multiplier()) * boostMultiplier;

        double bonus = Settings.KILLSTREAK_BONUS_EXP.getDouble();
        this.experience += bonus * this.killStreak;

        Player player = getPlayer().getPlayer();
        if ((currentLevel != getLevel() || currentLevel > getLevel()) && player != null) {
            for (int i = currentLevel + 1; i <= getLevel(); i++) {
                Level def = plugin.getLevelManager().getLevel(-1);
                if (def != null) {
                    Rewards.run(def.getRewards(), player, i, i == getLevel());
                }
                if (plugin.getLevelManager().getLevel(i) == null) {
                    continue;
                }
                Rewards.run(plugin.getLevelManager().getLevel(i).getRewards(), player, i, i == getLevel());
            }

            if (Settings.SEND_BROADCAST_LEVELUP_MESSAGE.getBoolean()
                    && getLevel() % Settings.BROADCAST_LEVELUP_EVERY.getInt() == 0) {
                for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != player).collect(Collectors.toList())) {
                    plugin.getLocale().getMessage("event.levelup.announcement")
                            .processPlaceholder("player", player.getName())
                            .processPlaceholder("level", getLevel())
                            .sendPrefixedMessage(pl);
                }
            }
        }
        if (this.experience > Settings.MAX_EXP.getDouble()) {
            this.experience = Settings.MAX_EXP.getDouble();
        }
        return this.experience;
    }

    public boolean canGainExperience(UUID uuid) {
        int triggerAmount = Settings.GRINDER_MAX.getInt();
        int maxInterval = Settings.GRINDER_INTERVAL.getInt() * 1000;

        long killCount = this.kills.keySet()
                .stream()
                .filter(x -> this.kills.get(x).equals(uuid))
                .filter(x -> System.currentTimeMillis() - x < maxInterval)
                .count();
        return killCount < triggerAmount;
    }

    private int multiplier() {
        int multiplier = 1;
        if (!getPlayer().isOnline()) {
            return multiplier;
        }
        for (PermissionAttachmentInfo permissionAttachmentInfo : getPlayer().getPlayer().getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epiclevels.multiplier")) {
                continue;
            }
            multiplier = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
        }
        return multiplier;
    }

    public double getExperience() {
        return this.experience;
    }

    public int getKills() {
        return this.mobKills + this.playerKills;
    }

    public int getMobKills() {
        return this.mobKills;
    }

    public int getPlayerKills() {
        return this.playerKills;
    }

    public int addMobKill() {
        this.saved = false;
        return this.mobKills++;
    }

    public int addPlayerKill(UUID uuid) {
        saved = false;
        this.kills.put(System.currentTimeMillis(), uuid);
        return this.playerKills++;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int addDeath() {
        this.saved = false;
        return this.deaths++;
    }

    public int getKillStreak() {
        return this.killStreak;
    }

    /**
     * @deprecated Use {@link #getKillStreak()} instead.
     */
    @Deprecated
    public int getKillstreak() {
        return getKillStreak();
    }

    public int getBestKillStreak() {
        return this.bestKillStreak;
    }

    /**
     * @deprecated Use {@link #getBestKillStreak()} instead.
     */
    @Deprecated
    public int getBestKillstreak() {
        return getBestKillStreak();
    }

    public int increaseKillStreak() {
        this.saved = false;
        this.killStreak++;
        if (this.killStreak > this.bestKillStreak) {
            this.bestKillStreak = this.killStreak;
        }
        return this.killStreak;
    }

    /**
     * @deprecated Use {@link #increaseKillStreak()} instead.
     */
    @Deprecated
    public int increaseKillstreak() {
        return increaseKillStreak();
    }

    public void resetKillStreak() {
        this.saved = false;
        this.killStreak = 0;
    }

    /**
     * @deprecated Use {@link #resetKillStreak()} instead.
     */
    @Deprecated
    public void resetKillstreak() {
        resetKillStreak();
    }

    public int getLevel() {
        int lastLevel = 0;
        for (int i = 1; i <= Settings.MAX_LEVEL.getInt(); i++) {
            if (experience(i) > this.experience) {
                break;
            }
            lastLevel++;
        }
        return lastLevel;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public static double experience(int level) {
        Formula formula = Formula.valueOf(Settings.LEVELING_FORMULA.getString());
        switch (formula) {
            case EXPONENTIAL: {
                double a = 0;
                for (int i = 1; i < level; i++) {
                    a += Math.floor(i + Settings.EXPONENTIAL_BASE.getDouble()
                            * Math.pow(2, (i / Settings.EXPONENTIAL_DIVISOR.getDouble())));
                }
                return Math.floor(a);
            }
            case LINEAR: {
                double a = 0;
                for (int i = 1; i < level; i++) {
                    a += Settings.LINEAR_INCREMENT.getDouble();
                }
                return a;
            }
            case CUSTOM: {
                return Math.round(MathUtils.eval(Settings.CUSTOM_FORMULA.getString()
                        .replace("level", String.valueOf(level))));
            }
        }
        return 0;
    }

    public enum Formula {
        LINEAR, EXPONENTIAL, CUSTOM
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EPlayer ePlayer = (EPlayer) obj;
        return Double.compare(ePlayer.experience, this.experience) == 0 &&
                this.mobKills == ePlayer.mobKills &&
                this.playerKills == ePlayer.playerKills &&
                this.deaths == ePlayer.deaths &&
                this.killStreak == ePlayer.killStreak &&
                this.bestKillStreak == ePlayer.bestKillStreak &&
                this.uuid.equals(ePlayer.uuid) &&
                this.kills.equals(ePlayer.kills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.experience, this.mobKills, this.playerKills, this.deaths, this.killStreak, this.bestKillStreak, this.kills);
    }

    @Override
    public String toString() {
        return "EPlayer{" +
                "uuid=" + uuid +
                ", experience=" + experience +
                ", mobKills=" + mobKills +
                ", playerKills=" + playerKills +
                ", deaths=" + deaths +
                ", killstreak=" + killstreak +
                ", bestKillstreak=" + bestKillstreak +
                ", kills=" + kills +
                ", saved=" + saved +
                '}';
    }
}
