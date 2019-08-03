package com.songoda.epiclevels;

import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.boost.BoostManager;
import com.songoda.epiclevels.command.CommandManager;
import com.songoda.epiclevels.database.*;
import com.songoda.epiclevels.economy.Economy;
import com.songoda.epiclevels.economy.PlayerPointsEconomy;
import com.songoda.epiclevels.economy.ReserveEconomy;
import com.songoda.epiclevels.economy.VaultEconomy;
import com.songoda.epiclevels.killstreaks.KillstreakManager;
import com.songoda.epiclevels.levels.LevelManager;
import com.songoda.epiclevels.listeners.DeathListeners;
import com.songoda.epiclevels.listeners.LoginListeners;
import com.songoda.epiclevels.placeholder.PlaceholderManager;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.players.PlayerManager;
import com.songoda.epiclevels.storage.Storage;
import com.songoda.epiclevels.storage.StorageRow;
import com.songoda.epiclevels.storage.types.StorageYaml;
import com.songoda.epiclevels.tasks.BoostTask;
import com.songoda.epiclevels.tasks.ModifierTask;
import com.songoda.epiclevels.utils.Methods;
import com.songoda.epiclevels.utils.Metrics;
import com.songoda.epiclevels.utils.ServerVersion;
import com.songoda.epiclevels.utils.gui.updateModules.LocaleModule;
import com.songoda.epiclevels.utils.locale.Locale;
import com.songoda.epiclevels.utils.settings.Setting;
import com.songoda.epiclevels.utils.settings.SettingsManager;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class EpicLevels extends JavaPlugin {
    private static CommandSender console = Bukkit.getConsoleSender();
    private static EpicLevels INSTANCE;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    private SettingsManager settingsManager;
    private PlayerManager playerManager;
    private CommandManager commandManager;
    private LevelManager levelManager;
    private KillstreakManager killstreakManager;
    private BoostManager boostManager;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

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
        settingsManager.setupConfig();

        // Setup language
        new Locale(this, "en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 44);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.playerManager = new PlayerManager();
        this.commandManager = new CommandManager(this);
        this.levelManager = new LevelManager();
        this.killstreakManager = new KillstreakManager();
        this.boostManager = new BoostManager();

        PluginManager pluginManager = getServer().getPluginManager();

        // Setup Economy
        if (Setting.VAULT_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("Vault"))
            this.economy = new VaultEconomy();
        else if (Setting.RESERVE_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("Reserve"))
            this.economy = new ReserveEconomy();
        else if (Setting.PLAYER_POINTS_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("PlayerPoints"))
            this.economy = new PlayerPointsEconomy();

        // Listener Registration
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Loading levels
        levelManager.load();

        // Loading killstreaks
        killstreakManager.load();

        // Start Tasks
        if (isServerVersionAtLeast(ServerVersion.V1_9)) ModifierTask.startTask(this);
        BoostTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI"))
            new PlaceholderManager(this).register();

        // Start Metrics
        new Metrics(this);

        // Load Legacy Data
        Bukkit.getScheduler().runTaskLater(this, this::loadLegacyData, 10);

        // Database stuff, go!
        try {
            if (Setting.MYSQL_ENABLED.getBoolean()) {
                String hostname = Setting.MYSQL_HOSTNAME.getString();
                int port = Setting.MYSQL_PORT.getInt();
                String database = Setting.MYSQL_DATABASE.getString();
                String username = Setting.MYSQL_USERNAME.getString();
                String password = Setting.MYSQL_PASSWORD.getString();
                boolean useSSL = Setting.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL);
                this.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this);
                this.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.dataManager = new DataManager(this.databaseConnector, this);
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager);
        this.dataMigrationManager.runMigrations();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.dataManager.getPlayers((player) -> this.playerManager.addPlayers(player));
            this.dataManager.getBoosts((uuidBoostMap -> this.boostManager.addBoosts(uuidBoostMap)));
        }, 20L);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    @Override
    public void onDisable() {
        this.dataManager.bulkUpdatePlayers(this.playerManager.getPlayers());
        this.dataManager.bulkUpdateBoosts(this.boostManager.getBoosts().values());
        this.dataManager.updateBoost(this.boostManager.getGlobalBoost());
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicLevels " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    public void reload() {
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));
        this.locale.reloadMessages();
        levelManager.load();
        settingsManager.reloadConfig();
    }

    private void loadLegacyData() {
        File folder = getDataFolder();
        File dataFile = new File(folder, "data.yml");

        if (!dataFile.exists()) return;
        Storage storage = new StorageYaml(this);
        if (storage.containsGroup("players")) {
            for (StorageRow row : storage.getRowsByGroup("players")) {
                if (row.get("uuid").asObject() == null)
                    continue;

                EPlayer player = new EPlayer(
                        UUID.fromString(row.get("uuid").asString()),
                        row.get("experience").asDouble(),
                        row.get("mobKills").asInt(),
                        row.get("playerKills").asInt(),
                        row.get("deaths").asInt(),
                        row.get("killstreak").asInt(),
                        row.get("bestKillstreak").asInt());
                getDataManager().createPlayer(player);

                this.playerManager.addPlayer(player);
            }
        }

        if (storage.containsGroup("boosts")) {
            for (StorageRow row : storage.getRowsByGroup("boosts")) {

                Boost boost = new Boost(row.get("expiration").asLong(), row.get("multiplier").asDouble());

                if (row.get("global") != null)
                    dataManager.createBoost(null, boost);
                else
                    dataManager.createBoost(UUID.fromString(row.get("uuid").asString()), boost);
            }
        }
        dataFile.delete();
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

    public KillstreakManager getKillstreakManager() {
        return killstreakManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
