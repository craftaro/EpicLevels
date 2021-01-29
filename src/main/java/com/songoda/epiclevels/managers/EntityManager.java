package com.songoda.epiclevels.managers;

import com.songoda.core.configuration.Config;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntityManager {

    private final Config mobConfig = new Config(EpicLevels.getInstance(), "entities.yml");
    private final Map<EntityType, Double> mobs = new HashMap<>();

    public EntityManager() {
        mobConfig.load();
        loadEntityFile();
        loadEntities();
    }

    public double getExperience(EntityType entityType) {
        return mobs.getOrDefault(entityType, 2.5);
    }

    private void loadEntityFile() {
        getEntities().forEach(entityType ->
                mobConfig.addDefault(entityType.name(), 2.5));

        mobConfig.options().copyDefaults(true);
        mobConfig.save();
    }

    public void reload() {
        mobConfig.load();
        loadEntityFile();

        mobs.clear();
        loadEntities();
    }

    private void loadEntities() {
        getEntities().forEach(entityType ->
                mobs.put(entityType, mobConfig.getDouble(entityType.name())));
    }

    private Stream<EntityType> getEntities() {
        return Arrays.stream(EntityType.values()).filter(entityType -> entityType != EntityType.PLAYER && entityType.isAlive());
    }
}
