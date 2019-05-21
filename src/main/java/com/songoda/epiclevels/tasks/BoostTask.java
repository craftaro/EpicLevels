package com.songoda.epiclevels.tasks;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.BoostManager;
import com.songoda.epiclevels.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BoostTask extends BukkitRunnable {

    private static BoostTask instance;
    private static EpicLevels plugin;

    public BoostTask(EpicLevels plug) {
        plugin = plug;
    }

    public static BoostTask startTask(EpicLevels plug) {
        plugin = plug;
        if (instance == null) {
            instance = new BoostTask(plugin);
            instance.runTaskTimer(plugin, 0, 20);
        }

        return instance;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player ->
                plugin.getBoostManager().getBoost(player.getUniqueId()));
    }
}
