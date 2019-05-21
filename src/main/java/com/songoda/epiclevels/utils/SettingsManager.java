package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by songo on 6/4/2017.
 */
public class SettingsManager implements Listener {

    private static final Pattern SETTINGS_PATTERN = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);
    private final EpicLevels instance;
    private String pluginName = "EpicLevels";
    private Map<Player, String> cat = new HashMap<>();
    private Map<Player, String> current = new HashMap<>();

    public SettingsManager(EpicLevels plugin) {
        this.instance = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getInventory() != event.getWhoClicked().getOpenInventory().getTopInventory()
                || clickedItem == null || !clickedItem.hasItemMeta()
                || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getView().getTitle().equals(pluginName + " Settings Manager")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            String type = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            this.cat.put((Player) event.getWhoClicked(), type);
            this.openEditor((Player) event.getWhoClicked());
        } else if (event.getView().getTitle().equals(pluginName + " Settings Editor")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            Player player = (Player) event.getWhoClicked();

            String key = cat.get(player) + "." + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (instance.getConfig().get(key).getClass().getName().equals("java.lang.Boolean")) {
                this.instance.getConfig().set(key, !instance.getConfig().getBoolean(key));
                this.finishEditing(player);
            } else {
                this.editObject(player, key);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!current.containsKey(player)) return;

        String value = current.get(player);
        FileConfiguration config = instance.getConfig();
        if (config.isInt(value)) {
            config.set(value, Integer.parseInt(event.getMessage()));
        } else if (config.isDouble(value)) {
            config.set(value, Double.parseDouble(event.getMessage()));
        } else if (config.isString(value)) {
            config.set(value, event.getMessage());
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(EpicLevels.getInstance(), () ->
                this.finishEditing(player), 0L);

        event.setCancelled(true);
    }

    private void finishEditing(Player player) {
        this.current.remove(player);
        this.instance.saveConfig();
        this.openEditor(player);
    }

    private void editObject(Player player, String current) {
        this.current.put(player, ChatColor.stripColor(current));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(Methods.formatText("&7Please enter a value for &6" + current + "&7."));
        if (instance.getConfig().isInt(current) || instance.getConfig().isDouble(current)) {
            player.sendMessage(Methods.formatText("&cUse only numbers."));
        }
        player.sendMessage("");
    }

    public void openSettingsManager(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, pluginName + " Settings Manager");
        ItemStack glass = Methods.getGlass();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        int slot = 10;
        for (String key : instance.getConfig().getDefaultSection().getKeys(false)) {
            ItemStack item = new ItemStack(instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.WHITE_WOOL : Material.valueOf("WOOL"), 1, (byte) (slot - 9)); //ToDo: Make this function as it was meant to.
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(Methods.formatText("&6Click To Edit This Category.")));
            meta.setDisplayName(Methods.formatText("&f&l" + key));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    private void openEditor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, pluginName + " Settings Editor");
        FileConfiguration config = instance.getConfig();

        int slot = 0;
        for (String key : config.getConfigurationSection(cat.get(player)).getKeys(true)) {
            String fKey = cat.get(player) + "." + key;
            ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Methods.formatText("&6" + key));

            List<String> lore = new ArrayList<>();
            if (config.isBoolean(fKey)) {
                item.setType(Material.LEVER);
                lore.add(Methods.formatText(config.getBoolean(fKey) ? "&atrue" : "&cfalse"));
            } else if (config.isString(fKey)) {
                item.setType(Material.PAPER);
                lore.add(Methods.formatText("&9" + config.getString(fKey)));
            } else if (config.isInt(fKey)) {
                item.setType(instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.CLOCK : Material.valueOf("WATCH"));
                lore.add(Methods.formatText("&5" + config.getInt(fKey)));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void updateSettings() {
        FileConfiguration config = instance.getConfig();

        for (Setting setting : Setting.values()) {
            config.addDefault(setting.setting, setting.option);
        }
    }

    public enum Setting {

        AUTOSAVE("Main.Auto Save Interval In Seconds", 15),
        EXP_MOB("Main.Experience Gained Per Mob Kill", 2L),
        EXP_PLAYER("Main.Experience Gained Per Player Kill", 250L),
        EXP_DEATH("Main.Experience lost On Death", 200L),
        ALLOW_NEGATIVE("Main.Allow Negative Experience", false),

        SEND_KILL_MESSAGE("Main.Send Message On Kill", true),
        SEND_DEATH_MESSAGE("Main.Send Message On Death", true),
        SEND_BROADCAST_DEATH_MESSAGE("Main.Send Broadcast On Death", true),
        SEND_BROADCAST_LEVELUP_MESSAGE("Main.Send Broadcast On Levelup", true),
        BROADCAST_LEVELUP_EVERY("Main.Broadcast Levelup Every", 5),

        MAX_LEVEL("Main.Max Level", 99),
        MAX_EXP("Main.Max Experience", 200000000L),

        START_EXP("Main.Starting Experience", 0L),

        START_PVP_LEVEL("Main.Level Required For PVP", 0),

        BLACKLISTED_WORLDS("Main.Blacklisted Worlds", Arrays.asList("World1", "World2", "World3")),

        MAX_EXTRA_HEALTH("Main.Max Extra Health", 10),
        EXTRA_HEALTH_PER_LEVEL("Main.Extra Health Per Level", 0.2),
        MAX_EXTRA_DAMAGE("Main.Max Extra Damage", 2.0),
        EXTRA_DAMAGE_PER_LEVEL("Main.Extra Damage Per Level", 0.05),

        RUN_KILLSTREAK_EVERY("Main.Run Killstreaks Reward Every", 3),
        KILLSTREAK_BONUS_EXP("Main.Killstreak Bonus Experience", 0.2),

        PROGRESS_BAR_LENGTH("Main.Progress Bar Length", 36),
        PROGRESS_BAR_LENGTH_PLACEHOLDER("Main.Progress Bar Length Placeholder", 20),

        BOOST_ITEMS("Boost.Items Enabled", true),
        BOOST_EXP("Boost.Experience Enabled", true),

        ANTI_GRINDER("Anti Grinder.Enabled", true),
        GRINDER_INTERVAL("Anti Grinder.Interval",900),
        GRINDER_MAX("Anti Grinder.Max Kills Before Trigger", 3),
        GRINDER_ALERT("Anti Grinder.Alert When Triggered", true),

        LEVELING_FORMULA("Formula.Leveling", "EXPONENTIAL"),
        LINEAR_INCREMENT("Formula.Linear Increment", 500),
        EXPONENTIAL_BASE("Formula.Exponential Base", 500),
        EXPONENTIAL_DIVISOR("Formula.Exponential Divisor", 7),
        CUSTOM_FORMULA("Formula.Custom", "Math.pow(1.5, level) * Math.sqrt(level) * 2;"),

        GLASS_TYPE_1("Interfaces.Glass Type 1", 7),
        GLASS_TYPE_2("Interfaces.Glass Type 2", 11),
        GLASS_TYPE_3("Interfaces.Glass Type 3", 3),

        LANGUGE_MODE("System.Language Mode", "en_US");

        private String setting;
        private Object option;

        Setting(String setting, Object option) {
            this.setting = setting;
            this.option = option;
        }

        public List<String> getStringList() {
            return EpicLevels.getInstance().getConfig().getStringList(setting);
        }

        public boolean getBoolean() {
            return EpicLevels.getInstance().getConfig().getBoolean(setting);
        }

        public int getInt() {
            return EpicLevels.getInstance().getConfig().getInt(setting);
        }

        public long getLong() {
            return EpicLevels.getInstance().getConfig().getLong(setting);
        }

        public String getString() {
            return EpicLevels.getInstance().getConfig().getString(setting);
        }

        public char getChar() { return EpicLevels.getInstance().getConfig().getString(setting).charAt(0); }

        public double getDouble() {
            return EpicLevels.getInstance().getConfig().getDouble(setting);
        }
    }
}