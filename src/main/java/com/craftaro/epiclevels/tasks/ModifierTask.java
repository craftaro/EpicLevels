package com.craftaro.epiclevels.tasks;

import com.craftaro.epiclevels.EpicLevels;
import com.craftaro.epiclevels.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ModifierTask extends BukkitRunnable {
    private static ModifierTask instance;
    private static EpicLevels plugin;

    public ModifierTask(EpicLevels plug) {
        plugin = plug;
    }

    public static ModifierTask startTask(EpicLevels plug) {
        plugin = plug;

        if (instance == null) {
            instance = new ModifierTask(plugin);
            instance.runTaskTimer(plugin, 40, 20);
        }
        return instance;
    }

    @Override
    public void run() {
        double healthPerLevel = Settings.EXTRA_HEALTH_PER_LEVEL.getDouble();
        double damagePerLevel = Settings.EXTRA_DAMAGE_PER_LEVEL.getDouble();
        Bukkit.getOnlinePlayers().forEach(player -> {
            updateHealthModifier(player, plugin.getPlayerManager().getPlayer(player).getLevel() * healthPerLevel);
            updateDamageModifier(player, plugin.getPlayerManager().getPlayer(player).getLevel() * damagePerLevel);
        });
    }

    private void updateHealthModifier(Player player, double health) {
        int maxHealth = Settings.MAX_EXTRA_HEALTH.getInt();
        if (health > maxHealth) {
            health = maxHealth;
        }

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        for (AttributeModifier modifier : healthAttribute.getModifiers()) {
            if (!modifier.getName().equals("EpicLevels")) {
                continue;
            }
            if (modifier.getAmount() == (int) health) {
                return;
            }

            healthAttribute.removeModifier(modifier);
        }
        healthAttribute.addModifier(new AttributeModifier("EpicLevels", (int) health, AttributeModifier.Operation.ADD_NUMBER));
    }

    private void updateDamageModifier(Player player, double damage) {
        double maxDamage = Settings.MAX_EXTRA_DAMAGE.getDouble();
        if (damage > maxDamage) {
            damage = maxDamage;
        }

        AttributeInstance damageAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        for (AttributeModifier modifier : damageAttribute.getModifiers()) {
            if (!modifier.getName().equals("EpicLevels")) {
                continue;
            }
            if (modifier.getAmount() == damage) {
                return;
            }

            damageAttribute.removeModifier(modifier);
        }
        damageAttribute.addModifier(new AttributeModifier("EpicLevels", damage, AttributeModifier.Operation.ADD_NUMBER));
    }
}
