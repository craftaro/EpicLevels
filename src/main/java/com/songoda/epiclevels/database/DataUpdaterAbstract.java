package com.songoda.epiclevels.database;

import com.songoda.core.database.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class DataUpdaterAbstract {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long currentID = -1;
    private boolean enabled = false;

    public void onEnable() {
        getConnector().connect(connection -> {
            // Taken from LuckPerms
            String createTable = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "messenger` (`id` INT AUTO_INCREMENT NOT NULL, `time` TIMESTAMP NOT NULL, `msg` TEXT NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4";
            try (Statement statement = connection.createStatement()) {
                try {
                    statement.execute(createTable);
                } catch (SQLException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        statement.execute(createTable.replace("utf8mb4", "utf8"));
                    } else {
                        throw e;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT MAX(`id`) as `latest` FROM `" + getTablePrefix() + "messenger`")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        this.currentID = resultSet.getLong("latest");
                    }
                }
            }
            this.enabled = true;
        });
    }

    public void onDisable() {
        this.enabled = false;
    }

    public abstract DatabaseConnector getConnector();

    public abstract String getTablePrefix();

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void sendMessage(String msg) {
        if (!isEnabled()) {
            return;
        }
        this.lock.readLock().lock();

        try {
            getConnector().connect(connection -> {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + getTablePrefix() + "messenger` (`time`, `msg`) VALUES(NOW(), ?)")) {
                    statement.setString(1, msg);
                    statement.execute();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.lock.readLock().unlock();
    }

    public void getMessages(Consumer<String> callback) {
        if (!isEnabled()) {
            return;
        }
        this.lock.readLock().lock();

        try {
            getConnector().connect(connection -> {
                try (PreparedStatement statement = connection.prepareStatement("SELECT `id`, `msg` FROM `" + getTablePrefix() + "messenger` WHERE `id` > ? AND (NOW() - `time` < 30)")) {
                    statement.setLong(1, this.currentID);
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            long id = rs.getLong("id");
                            this.currentID = Math.max(this.currentID, id);

                            String message = rs.getString("msg");
                            callback.accept(message);
                        }
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.lock.readLock().unlock();
    }

    public void cleanMessages() {
        if (!isEnabled()) {
            return;
        }
        this.lock.readLock().lock();

        try {
            getConnector().connect(connection -> {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + getTablePrefix() + "messenger` WHERE (NOW() - `time` > 60)")) {
                    statement.execute();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.lock.readLock().unlock();
    }
}
