package com.craftaro.epiclevels;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.epiclevels.boost.BoostManager;
import com.craftaro.epiclevels.commands.CommandAddExp;
import com.craftaro.epiclevels.commands.CommandBoost;
import com.craftaro.epiclevels.commands.CommandEpicLevels;
import com.craftaro.epiclevels.commands.CommandGlobalBoost;
import com.craftaro.epiclevels.commands.CommandHelp;
import com.craftaro.epiclevels.commands.CommandReload;
import com.craftaro.epiclevels.commands.CommandRemoveBoost;
import com.craftaro.epiclevels.commands.CommandRemoveGlobalBoost;
import com.craftaro.epiclevels.commands.CommandReset;
import com.craftaro.epiclevels.commands.CommandSettings;
import com.craftaro.epiclevels.commands.CommandShow;
import com.craftaro.epiclevels.commands.CommandTakeExp;
import com.craftaro.epiclevels.database.DataHelper;
import com.craftaro.epiclevels.database.migrations._1_InitialMigration;
import com.craftaro.epiclevels.killstreaks.KillstreakManager;
import com.craftaro.epiclevels.levels.LevelManager;
import com.craftaro.epiclevels.listeners.DeathListeners;
import com.craftaro.epiclevels.listeners.LoginListeners;
import com.craftaro.epiclevels.managers.EntityManager;
import com.craftaro.epiclevels.placeholder.PlaceholderManager;
import com.craftaro.epiclevels.players.PlayerManager;
import com.craftaro.epiclevels.settings.Settings;
import com.craftaro.epiclevels.tasks.BoostTask;
import com.craftaro.epiclevels.tasks.ModifierTask;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EpicLevels extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private PlayerManager playerManager;
    private CommandManager commandManager;
    private LevelManager levelManager;
    private KillstreakManager killstreakManager;
    private EntityManager entityManager;
    private BoostManager boostManager;

    private DataHelper dataHelper;

    /**
     * @deprecated Use {@link #getPlugin(Class)} instead
     */
    @Deprecated
    public static EpicLevels getInstance() {
        return getPlugin(EpicLevels.class);
    }

    @Override
    public void onPluginLoad() {
    }

    @Override
    public void onPluginDisable() {
        this.dataHelper.getUpdater().onDisable();
        this.dataHelper.bulkUpdatePlayers(this.playerManager.getPlayers());
        this.dataHelper.bulkUpdateBoosts(this.boostManager.getBoosts().values());

        if (this.boostManager.getGlobalBoost() != null) {
            this.dataHelper.updateBoost(this.boostManager.getGlobalBoost());
        }
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 44, XMaterial.NETHER_STAR);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Set Economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY.getString());

        // Listener Registration
        this.guiManager.init();
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Load Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandEpicLevels(this.guiManager))
                .addSubCommands(
                        new CommandAddExp(this),
                        new CommandBoost(this),
                        new CommandGlobalBoost(this),
                        new CommandHelp(this),
                        new CommandReload(this),
                        new CommandRemoveBoost(this),
                        new CommandRemoveGlobalBoost(this),
                        new CommandReset(this),
                        new CommandSettings(this.guiManager, this),
                        new CommandShow(this),
                        new CommandTakeExp(this)
                );

        // Load Managers
        this.playerManager = new PlayerManager(this);
        this.levelManager = new LevelManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.entityManager = new EntityManager(this);
        this.boostManager = new BoostManager(this);

        // Loading levels
        this.levelManager.load(this);

        // Loading kill streaks
        this.killstreakManager.load(this);

        // Start Tasks
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
            ModifierTask.startTask(this);
        }
        BoostTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderManager(this).register();
        }
    }

    @Override
    public void onDataLoad() {
        initDatabase(Collections.singletonList(new _1_InitialMigration(this)));
        this.dataHelper = new DataHelper(this);

        this.dataHelper.getPlayers((player) -> this.playerManager.addPlayers(player));
        this.dataHelper.getBoosts((uuidBoostMap -> this.boostManager.addBoosts(uuidBoostMap)));
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();

        // Loading levels
        this.levelManager.load(this);
        // Loading kill streaks
        this.killstreakManager.load(this);
        // Loading entities.
        this.entityManager.reload();

        this.levelManager.load(this);
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(this.levelManager.getLevelsConfig(), this.killstreakManager.getKillstreaksConfig());
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    public KillstreakManager getKillstreakManager() {
        return this.killstreakManager;
    }

    public BoostManager getBoostManager() {
        return this.boostManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return super.getDataManager().getDatabaseConnector();
    }

    public DataHelper getDataHelper() {
        return this.dataHelper;
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }
}
