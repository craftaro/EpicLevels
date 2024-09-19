package com.craftaro.epiclevels.tasks;

import com.craftaro.core.compatibility.MajorServerVersion;
import com.craftaro.epiclevels.EpicLevels;
import com.craftaro.epiclevels.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
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
        double maxHealth = Settings.MAX_EXTRA_HEALTH.getDouble();
        if (health > maxHealth) {
            health = maxHealth;
        }

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) {
            return;
        }

        // Remove the old "EpicLevels" modifier if present
        healthAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getName().equals("EpicLevels"))
                .findFirst()
                .ifPresent(healthAttribute::removeModifier);

        // Add the new "EpicLevels" modifier based on server version
        if (MajorServerVersion.isServerVersionAtLeast(MajorServerVersion.V1_20)) {
            applyNewHealthModifier(player, health);
        } else {
            healthAttribute.addModifier(new AttributeModifier("EpicLevels", (int) health, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    private void applyNewHealthModifier(Player player, double health) {
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) {
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "EpicLevels");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.HEAD;

        // Remove the existing modifier by key to avoid duplication
        healthAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .findFirst()
                .ifPresent(healthAttribute::removeModifier);

        // Apply new modifier
        AttributeModifier newModifier = new AttributeModifier(key, health, AttributeModifier.Operation.ADD_NUMBER, slotGroup);
        healthAttribute.addModifier(newModifier);
    }

    private void updateDamageModifier(Player player, double damage) {
        double maxDamage = Settings.MAX_EXTRA_DAMAGE.getDouble();
        if (damage > maxDamage) {
            damage = maxDamage;
        }

        AttributeInstance damageAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttribute == null) {
            return;
        }

        // Remove the old "EpicLevels" modifier if present
        damageAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getName().equals("EpicLevels"))
                .findFirst()
                .ifPresent(damageAttribute::removeModifier);

        // Add the new "EpicLevels" modifier based on server version
        if (MajorServerVersion.isServerVersionAtLeast(MajorServerVersion.V1_20)) {
            applyNewDamageModifier(player, damage);
        } else {
            damageAttribute.addModifier(new AttributeModifier("EpicLevels", damage, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    private void applyNewDamageModifier(Player player, double damage) {
        AttributeInstance damageAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttribute == null) {
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "EpicLevels");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.MAINHAND;

        // Remove any existing modifier by key to avoid duplication
        damageAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .findFirst()
                .ifPresent(damageAttribute::removeModifier);

        // Apply new modifier
        AttributeModifier newModifier = new AttributeModifier(key, damage, AttributeModifier.Operation.ADD_NUMBER, slotGroup);
        damageAttribute.addModifier(newModifier);
    }
}
