package com.songoda.epiclevels.settings;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.configuration.ConfigSetting;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epiclevels.EpicLevels;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Settings {

    static final Config config = EpicLevels.getInstance().getCoreConfig();

    public static final ConfigSetting EXP_PLAYER = new ConfigSetting(config, "Main.Experience Gained Per Player Kill", 250,
            "The amount of experience gained per player kill.");

    public static final ConfigSetting EXP_DEATH = new ConfigSetting(config, "Main.Experience lost On Death", 200,
            "The amount of experience lost when a player is killed",
            "by another player.");

    public static final ConfigSetting ALLOW_NEGATIVE = new ConfigSetting(config, "Main.Allow Negative Experience", false,
            "Allow players to lose experience into negative?");

    public static final ConfigSetting SEND_PLAYER_KILL_MESSAGE = new ConfigSetting(config, "Main.Send Message On Player Kill", true,
            "Should Players be notified when they kill another player?");

    public static final ConfigSetting SEND_MOB_KILL_MESSAGE = new ConfigSetting(config, "Main.Send Message On Mob Kill", true,
            "Should Players be notified when they kill a mob?");

    public static final ConfigSetting SEND_DEATH_MESSAGE = new ConfigSetting(config, "Main.Send Message On Death", true,
            "Should players be notified when killed?");

    public static final ConfigSetting SEND_BROADCAST_DEATH_MESSAGE = new ConfigSetting(config, "Main.Send Broadcast On Death", true,
            "Broadcasts a players death to the whole server.");

    public static final ConfigSetting SEND_BROADCAST_LEVELUP_MESSAGE = new ConfigSetting(config, "Main.Send Broadcast On Levelup", true,
            "Broadcasts a players level up to the whole server.");

    public static final ConfigSetting SEND_BROADCAST_BROKEN_KILLSTREAK = new ConfigSetting(config, "Main.Send Broadcast On Broken Killstreak", true,
            "Broadcasts a broken killstreak to the whole server.");

    public static final ConfigSetting SEND_KILLSTREAK_BROKEN_MESSAGE = new ConfigSetting(config, "Main.Send Killstreak Broken Message", true,
            "Should players be notified when a killstreak is broken?");

    public static final ConfigSetting SEND_KILLSTREAK_ALERTS_AFTER = new ConfigSetting(config, "Main.Send Killstreak Alerts After", 5,
            "Kill streak message and broadcasts will only display if a",
            "killstreak of this value or greater is reached.");

    public static final ConfigSetting BROADCAST_LEVELUP_EVERY = new ConfigSetting(config, "Main.Broadcast Levelup Every", 5,
            "How often should a level up be announced?",
            "If you enter 5 every 5 levels players will be notified.");

    public static final ConfigSetting MAX_LEVEL = new ConfigSetting(config, "Main.Max Level", 99,
            "The maximum allowed level.",
            "Note that once reached players will still earn experience beyond",
            "the the maximum level without the ability to level up.");

    public static final ConfigSetting MAX_EXP = new ConfigSetting(config, "Main.Max Experience", 200000000,
            "The maximum allowed experience.");

    public static final ConfigSetting START_EXP = new ConfigSetting(config, "Main.Starting Experience", 0,
            "The amount of experience players start with.");

    public static final ConfigSetting START_PVP_LEVEL = new ConfigSetting(config, "Main.Level Required For PVP", 0,
            "The minimum level required to engage or be engaged with in combat.");

    public static final ConfigSetting BLACKLISTED_WORLDS = new ConfigSetting(config, "Main.Blacklisted Worlds", Arrays.asList("World1", "World2", "World3"),
            "Worlds that kills are not counted in.");

    public static final ConfigSetting MAX_EXTRA_HEALTH = new ConfigSetting(config, "Main.Max Extra Health", 10,
            "The maximum amount of health a player can gain through levels.");

    public static final ConfigSetting EXTRA_HEALTH_PER_LEVEL = new ConfigSetting(config, "Main.Extra Health Per Level", 0.2,
            "The amount of health a player will gain per level.",
            "1 would be half a heart and 20 would be a 10 hearts.");

    public static final ConfigSetting MAX_EXTRA_DAMAGE = new ConfigSetting(config, "Main.Max Extra Damage", 2.0,
            "The maximum amount of extra damage applied to a players attack",
            "per level.");

    public static final ConfigSetting EXTRA_DAMAGE_PER_LEVEL = new ConfigSetting(config, "Main.Extra Damage Per Level", 0.05,
            "The amount of additional experience applied to a player per",
            "level.");

    public static final ConfigSetting RUN_KILLSTREAK_EVERY = new ConfigSetting(config, "Main.Run Killstreaks Reward Every", 3,
            "A player must achieve a multiple of the following number in order for the reward",
            "script to run.",
            "You can set this to 1 if you would like the script to be ran with every kill.");

    public static final ConfigSetting KILLSTREAK_BONUS_EXP = new ConfigSetting(config, "Main.Killstreak Bonus Experience", 0.2,
            "For each kill in a killstreak the following number will be used to boost your",
            "experience gain (streak * exp).");

    public static final ConfigSetting PROGRESS_BAR_LENGTH = new ConfigSetting(config, "Main.Progress Bar Length", 36,
            "The length of the progress bar in the levels GUI.");

    public static final ConfigSetting PROGRESS_BAR_LENGTH_PLACEHOLDER = new ConfigSetting(config, "Main.Progress Bar Length Placeholder", 20,
            "The length of the progress bar in placeholders.");

    public static final ConfigSetting ECONOMY = new ConfigSetting(config, "Main.Economy",
            EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "You can choose from \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting ANTI_GRINDER = new ConfigSetting(config, "Anti Grinder.Enabled", true,
            "Enabling this will enable the anti grinder timeout.",
            "In its default configuration it will prevent a player from killing the same player",
            "more than 3 times in 15 minutes.");

    public static final ConfigSetting GRINDER_INTERVAL = new ConfigSetting(config, "Anti Grinder.Interval", 900,
            "How long in seconds should a player have to wait once triggered?");

    public static final ConfigSetting GRINDER_MAX = new ConfigSetting(config, "Anti Grinder.Max Kills Before Trigger", 3,
            "How many kills before trigger?");

    public static final ConfigSetting GRINDER_ALERT = new ConfigSetting(config, "Anti Grinder.Alert When Triggered", true,
            "Should we alert the killer when they have reached the threshold?");

    public static final ConfigSetting LEVELING_FORMULA = new ConfigSetting(config, "Formula.Leveling", "EXPONENTIAL",
            "This is the formula used when calculating a players level",
            "", "LINEAR: All levels require the same amount of experience to reach the next level.",
            "EXPONENTIAL: Levels will increasingly require more experience the higher level you are.",
            "CUSTOM: You can enter a custom JavaScript equation here to finely tune your experience gain.");

    public static final ConfigSetting LINEAR_INCREMENT = new ConfigSetting(config, "Formula.Linear Increment", 500,
            "The stagnant amount of experience required to level up.");

    public static final ConfigSetting EXPONENTIAL_BASE = new ConfigSetting(config, "Formula.Exponential Base", 500,
            "You need to know what you're doing to mess with this, feel",
            "free to put in random values and test around however.");

    public static final ConfigSetting EXPONENTIAL_DIVISOR = new ConfigSetting(config, "Formula.Exponential Divisor", 7,
            "You need to know what you're doing to mess with this, feel",
            "free to put in random values and test around however.");

    public static final ConfigSetting CUSTOM_FORMULA = new ConfigSetting(config, "Formula.Custom", "Math.pow(1.5, level) * Math.sqrt(level) * 2;",
            "This is the custom formula. You can use any JavaScript math functions here.");

    public static final ConfigSetting GLASS_TYPE_1 = new ConfigSetting(config, "Interfaces.Glass Type 1", "GRAY_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_2 = new ConfigSetting(config, "Interfaces.Glass Type 2", "BLUE_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_3 = new ConfigSetting(config, "Interfaces.Glass Type 3", "LIGHT_BLUE_STAINED_GLASS_PANE");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    public static final ConfigSetting MYSQL_ENABLED = new ConfigSetting(config, "MySQL.Enabled", false, "Set to 'true' to use MySQL instead of SQLite for data storage.");
    public static final ConfigSetting MYSQL_HOSTNAME = new ConfigSetting(config, "MySQL.Hostname", "localhost");
    public static final ConfigSetting MYSQL_PORT = new ConfigSetting(config, "MySQL.Port", 3306);
    public static final ConfigSetting MYSQL_DATABASE = new ConfigSetting(config, "MySQL.Database", "your-database");
    public static final ConfigSetting MYSQL_USERNAME = new ConfigSetting(config, "MySQL.Username", "user");
    public static final ConfigSetting MYSQL_PASSWORD = new ConfigSetting(config, "MySQL.Password", "pass");
    public static final ConfigSetting MYSQL_USE_SSL = new ConfigSetting(config, "MySQL.Use SSL", false);
    public static final ConfigSetting MYSQL_POOL_SIZE = new ConfigSetting(config, "MySQL.Pool Size", 3, "Determines the number of connections the pool is using. Increase this value if you are getting timeout errors when more players online.");

    /**
     * In order to set dynamic economy comment correctly, this needs to be
     * called after EconomyManager load
     */
    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        // convert glass pane settings
        int color;
        if ((color = GLASS_TYPE_1.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_1.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }
        if ((color = GLASS_TYPE_2.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_2.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }
        if ((color = GLASS_TYPE_3.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_3.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }

        // convert economy settings
        if (config.getBoolean("Economy.Use Vault Economy") && EconomyManager.getManager().isEnabled("Vault")) {
            config.set("Main.Economy", "Vault");
        } else if (config.getBoolean("Economy.Use Reserve Economy") && EconomyManager.getManager().isEnabled("Reserve")) {
            config.set("Main.Economy", "Reserve");
        } else if (config.getBoolean("Economy.Use Player Points Economy") && EconomyManager.getManager().isEnabled("PlayerPoints")) {
            config.set("Main.Economy", "PlayerPoints");
        }

        config.saveChanges();
    }
}
