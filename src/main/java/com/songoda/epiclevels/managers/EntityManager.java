package com.songoda.epiclevels.managers;

import com.songoda.core.configuration.Config;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntityManager {
    private final Config mobConfig;
    private final Map<EntityType, Double> mobs = new HashMap<>();

    public EntityManager(Plugin plugin) {
        this.mobConfig = new Config(plugin, "entities.yml");
        this.mobConfig.load();

        loadEntityFile();
        loadEntities();
    }

    public double getExperience(EntityType entityType) {
        return this.mobs.getOrDefault(entityType, 2.5);
    }

    private void loadEntityFile() {
        getEntities().forEach(entityType -> this.mobConfig.addDefault(entityType.name(), 2.5));

        this.mobConfig.options().copyDefaults(true);
        this.mobConfig.save();
    }

    public void reload() {
        this.mobConfig.load();
        loadEntityFile();

        this.mobs.clear();
        loadEntities();
    }

    private void loadEntities() {
        getEntities().forEach(entityType ->
                this.mobs.put(entityType, this.mobConfig.getDouble(entityType.name())));
    }

    private Stream<EntityType> getEntities() {
        return Arrays.stream(EntityType.values()).filter(entityType -> entityType != EntityType.PLAYER && entityType.isAlive());
    }
}
