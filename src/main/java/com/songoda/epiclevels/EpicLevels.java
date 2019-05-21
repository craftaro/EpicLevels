package com.songoda.epiclevels;

import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.boost.BoostManager;
import com.songoda.epiclevels.command.CommandManager;
import com.songoda.epiclevels.economy.Economy;
import com.songoda.epiclevels.economy.VaultEconomy;
import com.songoda.epiclevels.levels.LevelManager;
import com.songoda.epiclevels.listeners.DeathListeners;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.players.PlayerManager;
import com.songoda.epiclevels.storage.Storage;
import com.songoda.epiclevels.storage.StorageRow;
import com.songoda.epiclevels.storage.types.StorageYaml;
import com.songoda.epiclevels.tasks.BoostTask;
import com.songoda.epiclevels.tasks.ModifierTask;
import com.songoda.epiclevels.utils.Methods;
import com.songoda.epiclevels.utils.ServerVersion;
import com.songoda.epiclevels.utils.SettingsManager;
import com.songoda.epiclevels.utils.gui.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EpicLevels extends JavaPlugin {
    private static CommandSender console = Bukkit.getConsoleSender();
    private static EpicLevels INSTANCE;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    private SettingsManager settingsManager;
    private PlayerManager playerManager;
    private CommandManager commandManager;
    private LevelManager levelManager;
    private BoostManager boostManager;

    private References references;
    private Storage storage;
    private Economy economy;
    private Locale locale;

    public static EpicLevels getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicLevels " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.setupConfig();

        // Setup language
        String langMode = SettingsManager.Setting.LANGUGE_MODE.getString();
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 44);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.references = new References();

        this.playerManager = new PlayerManager();
        this.commandManager = new CommandManager(this);
        this.levelManager = new LevelManager();
        this.boostManager = new BoostManager();
        this.storage = new StorageYaml(this);


        if (getServer().getPluginManager().getPlugin("Vault") != null)
            this.economy = new VaultEconomy(this);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listener Registration
        pluginManager.registerEvents(new DeathListeners(this), this);

        // Loading levels
        levelManager.load();

        // Load Data
        loadData();

        // Start Tasks
        if (isServerVersionAtLeast(ServerVersion.V1_9)) ModifierTask.startTask(this);
        BoostTask.startTask(this);

        int timeout = SettingsManager.Setting.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    @Override
    public void onDisable() {
        saveToFile();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicLevels " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void saveToFile() {
        storage.doSave();
    }

    public void reload() {
        saveToFile();
        locale.reloadMessages();
        references = new References();
        levelManager.load();
        this.setupConfig();
        saveConfig();
    }

    private void setupConfig() {
        settingsManager.updateSettings();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    private void loadData() {
        if (storage.containsGroup("players")) {
            for (StorageRow row : storage.getRowsByGroup("players")) {
                if (row.get("uuid").asObject() == null)
                    continue;

                EPlayer player = new EPlayer(
                        UUID.fromString(row.get("uuid").asString()),
                        row.get("experience").asLong(),
                        row.get("mobKills").asInt(),
                        row.get("playerKills").asInt(),
                        row.get("deaths").asInt(),
                        row.get("killstreak").asInt(),
                        row.get("bestKillstreak").asInt());

                this.playerManager.addPlayer(player);
            }
        }
        if (storage.containsGroup("boosts")) {
            for (StorageRow row : storage.getRowsByGroup("boosts")) {

                Boost boost = new Boost(row.get("expiration").asLong(), row.get("multiplier").asDouble());

                if (row.get("global") != null)
                    boostManager.setGlobalBoost(boost);
                else
                    boostManager.addBoost(UUID.fromString(row.get("uuid").asString()), boost);
            }
        }

        // Save data initially so that if the person reloads again fast they don't lose all their data.
        this.saveToFile();
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }

    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    public Locale getLocale() {
        return locale;
    }

    public References getReferences() {
        return references;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }
}
