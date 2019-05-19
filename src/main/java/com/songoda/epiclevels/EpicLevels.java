package com.songoda.epiclevels;

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
import com.songoda.epiclevels.tasks.ModifierTask;
import com.songoda.epiclevels.utils.Methods;
import com.songoda.epiclevels.utils.ServerVersion;
import com.songoda.epiclevels.utils.SettingsManager;
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

        this.references = new References();

        this.playerManager = new PlayerManager();
        this.commandManager = new CommandManager(this);
        this.levelManager = new LevelManager();
        this.boostManager = new BoostManager();
        this.storage = new StorageYaml(this);


        if (getServer().getPluginManager().getPlugin("Vault") != null)
            this.economy = new VaultEconomy(this);

        playerManager.addPlayer(new EPlayer(UUID.fromString("be8c08d2-9c2e-4ed1-a1c1-e24ad05b6e7f"), 1173L, 35, 6, 2, 1, 74));
        playerManager.addPlayer(new EPlayer(UUID.fromString("c28b7d05-e20e-3fdd-9ba3-3c9599dc2bed"), 1123L, 3, 6, 62, 0, 0));
        playerManager.addPlayer(new EPlayer(UUID.fromString("38edcc03-328d-4a6c-a783-b0e579bc98fe"), 1347L, 3, 64, 2, 0, 0));
        playerManager.addPlayer(new EPlayer(UUID.fromString("845b66e1-45b8-4bc4-b0bd-d1ccdf0fc7a4"), 1342L, 3, 6, 22, 0, 0));
        playerManager.addPlayer(new EPlayer(UUID.fromString("9db1c64c-5459-43bc-802c-c475e227a271"), 1345L, 31, 61, 2, 32, 32));
        playerManager.addPlayer(new EPlayer(UUID.fromString("792cc008-277a-48aa-a444-977e91985ce6"), 1657L, 3, 6, 2, 0, 0));
        playerManager.addPlayer(new EPlayer(UUID.fromString("2f50536e-fc88-3dc6-a726-230a6cff6580"), 1765L, 3, 16, 22, 45, 45));
        playerManager.addPlayer(new EPlayer(UUID.fromString("8035aba9-973f-485d-a19f-ec1397137dd9"), 1876L, 13, 6, 2, 1, 1));
        playerManager.addPlayer(new EPlayer(UUID.fromString("79d19a5a-28df-319a-9aff-459c81cc9efc"), 1245L, 3, 6, 2, 4, 4));
        playerManager.addPlayer(new EPlayer(UUID.fromString("97fb7165-9e8d-3f22-aad2-7b617785eca0"), 1569L, 3, 6, 23, 1, 1));

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listener Registration
        pluginManager.registerEvents(new DeathListeners(this), this);

        // Loading levels
        levelManager.load();

        // Load Data
        loadData();

        // Start Tasks
        ModifierTask.startTask(this);

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
        // Adding in favorites.
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
