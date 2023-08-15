package com.craftaro.epiclevels.database.migrations;

import com.craftaro.core.database.DataMigration;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.database.MySQLConnector;

import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {
    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create 'players' table
        try (Statement statement = connector.getConnection().createStatement()) {
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
        try (Statement statement = connector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosts (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "uuid VARCHAR(36), " +
                    "expiration BIGINT NOT NULL," +
                    "multiplier DOUBLE NOT NULL " +
                    ")");
        }
    }
}
