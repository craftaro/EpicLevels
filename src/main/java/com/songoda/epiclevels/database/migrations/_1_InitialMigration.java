package com.songoda.epiclevels.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.epiclevels.EpicLevels;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {
    private final EpicLevels plugin;

    public _1_InitialMigration(EpicLevels plugin) {
        super(1);

        this.plugin = plugin;
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = this.plugin.getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create 'players' table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "experience DOUBLE NOT NULL," +
                    "mob_kills INTEGER NOT NULL, " +
                    "player_kills INTEGER NOT NULL, " +
                    "deaths INTEGER NOT NULL, " +
                    "killstreak INTEGER NOT NULL, " +
                    "best_killstreak INTEGER NOT NULL " +
                    ")");
        }

        // Create 'boosts' table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosts (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "uuid VARCHAR(36), " +
                    "expiration BIGINT NOT NULL," +
                    "multiplier DOUBLE NOT NULL " +
                    ")");
        }
    }
}
