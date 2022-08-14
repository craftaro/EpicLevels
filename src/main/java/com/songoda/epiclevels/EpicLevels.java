package com.songoda.epiclevels;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epiclevels.boost.BoostManager;
import com.songoda.epiclevels.commands.CommandAddExp;
import com.songoda.epiclevels.commands.CommandBoost;
import com.songoda.epiclevels.commands.CommandEpicLevels;
import com.songoda.epiclevels.commands.CommandGlobalBoost;
import com.songoda.epiclevels.commands.CommandHelp;
import com.songoda.epiclevels.commands.CommandReload;
import com.songoda.epiclevels.commands.CommandRemoveBoost;
import com.songoda.epiclevels.commands.CommandRemoveGlobalBoost;
import com.songoda.epiclevels.commands.CommandReset;
import com.songoda.epiclevels.commands.CommandSettings;
import com.songoda.epiclevels.commands.CommandShow;
import com.songoda.epiclevels.commands.CommandTakeExp;
import com.songoda.epiclevels.database.DataManager;
import com.songoda.epiclevels.database.migrations._1_InitialMigration;
import com.songoda.epiclevels.killstreaks.KillstreakManager;
import com.songoda.epiclevels.levels.LevelManager;
import com.songoda.epiclevels.listeners.DeathListeners;
import com.songoda.epiclevels.listeners.LoginListeners;
import com.songoda.epiclevels.managers.EntityManager;
import com.songoda.epiclevels.placeholder.PlaceholderManager;
import com.songoda.epiclevels.players.PlayerManager;
import com.songoda.epiclevels.settings.Settings;
import com.songoda.epiclevels.tasks.BoostTask;
import com.songoda.epiclevels.tasks.ModifierTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.List;

public class EpicLevels extends SongodaPlugin {

    private static EpicLevels INSTANCE;

    private final GuiManager guiManager = new GuiManager(this);
    private PlayerManager playerManager;
    private CommandManager commandManager;
    private LevelManager levelManager;
    private KillstreakManager killstreakManager;
    private EntityManager entityManager;
    private BoostManager boostManager;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

    public static EpicLevels getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        this.dataManager.getUpdater().onDisable();
        this.dataManager.bulkUpdatePlayers(this.playerManager.getPlayers());
        this.dataManager.bulkUpdateBoosts(this.boostManager.getBoosts().values());

        if (this.boostManager.getGlobalBoost() != null) {
            this.dataManager.updateBoost(this.boostManager.getGlobalBoost());
        }
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 44, CompatibleMaterial.NETHER_STAR);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Set Economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY.getString());

        // Listener Registration
        guiManager.init();
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Load Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandEpicLevels(guiManager))
                .addSubCommands(
                        new CommandAddExp(this),
                        new CommandBoost(this),
                        new CommandGlobalBoost(this),
                        new CommandHelp(this),
                        new CommandReload(this),
                        new CommandRemoveBoost(this),
                        new CommandRemoveGlobalBoost(this),
                        new CommandReset(this),
                        new CommandSettings(guiManager),
                        new CommandShow(this),
                        new CommandTakeExp(this)
                );

        // Load Managers
        this.playerManager = new PlayerManager();
        this.levelManager = new LevelManager();
        this.killstreakManager = new KillstreakManager();
        this.entityManager = new EntityManager();
        this.boostManager = new BoostManager();

        // Loading levels
        levelManager.load(this);

        // Loading killstreaks
        killstreakManager.load(this);

        // Start Tasks
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) ModifierTask.startTask(this);
        BoostTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI"))
            new PlaceholderManager(this).register();
    }

    @Override
    public void onDataLoad() {
        // Database stuff, go!
        try {
            if (Settings.MYSQL_ENABLED.getBoolean()) {
                String hostname = Settings.MYSQL_HOSTNAME.getString();
                int port = Settings.MYSQL_PORT.getInt();
                String database = Settings.MYSQL_DATABASE.getString();
                String username = Settings.MYSQL_USERNAME.getString();
                String password = Settings.MYSQL_PASSWORD.getString();
                boolean useSSL = Settings.MYSQL_USE_SSL.getBoolean();
                int poolSize = Settings.MYSQL_POOL_SIZE.getInt();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL, poolSize);
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
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration());
        this.dataMigrationManager.runMigrations();

        this.dataManager.getPlayers((player) -> this.playerManager.addPlayers(player));
        this.dataManager.getBoosts((uuidBoostMap -> this.boostManager.addBoosts(uuidBoostMap)));
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();

        // Loading levels
        levelManager.load(this);
        // Loading killstreaks
        killstreakManager.load(this);
        // Loading entities.
        entityManager.reload();

        levelManager.load(this);
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(levelManager.getLevelsConfig(), killstreakManager.getKillstreaksConfig());
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
