package com.songoda.epiclevels.storage;

import org.bukkit.Material;

import java.util.List;

public class StorageItem {

    private final Object object;
    private String key = null;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public StorageItem(String key, List<Material> material) {
        String object = "";
        for (Material m : material) {
            object += m.name() + ";";
        }
        this.key = key;
        this.object = object;
    }

    public String getKey() {
        return key;
    }

    public String asString() {
        if (object == null) return null;
        return (String) object;
    }

    public long asLong() {
        if (object == null) return -1;
        if (object instanceof Integer) {
            return (long)((int)object);
        }
        return (long) object;
    }

    public boolean asBoolean() {
        if (object == null) return false;
        return (boolean) object;
    }

    public int asInt() {
        if (object == null) return 0;
        return (int) object;
    }

    public double asDouble() {
        if (object == null) return 0;
        return (double) object;
    }

    public Object asObject() {
        return object;
    }

}
