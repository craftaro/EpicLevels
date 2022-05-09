package com.songoda.epiclevels.database;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.players.EPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    private final DataUpdater updater;

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
        this.updater = new DataUpdater(this);

        if (databaseConnector instanceof MySQLConnector) {
            this.updater.onEnable();
        }
    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public void close() {
        updater.onDisable();
    }

    public DataUpdater getUpdater() {
        return updater;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.plugin.getDescription().getName().toLowerCase() + '_';
    }

    public void bulkUpdatePlayers(Collection<EPlayer> ePlayers) {
        this.databaseConnector.connect(connection -> {
            String updatePlayer = "UPDATE " + this.getTablePrefix() + "players SET experience = ?, mob_kills = ?, player_kills = ?, deaths = ?, killstreak = ?, best_killstreak = ? WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(updatePlayer)) {
                for (EPlayer ePlayer : ePlayers) {
                    statement.setDouble(1, ePlayer.getExperience());

                    statement.setInt(2, ePlayer.getMobKills());
                    statement.setInt(3, ePlayer.getPlayerKills());
                    statement.setInt(4, ePlayer.getDeaths());
                    statement.setInt(5, ePlayer.getKillstreak());
                    statement.setInt(6, ePlayer.getBestKillstreak());

                    statement.setString(7, ePlayer.getUniqueId().toString());
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });
    }

    public void updatePlayer(EPlayer ePlayer) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updatePlayer = "UPDATE " + this.getTablePrefix() + "players SET experience = ?, mob_kills = ?, player_kills = ?, deaths = ?, killstreak = ?, best_killstreak = ? WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(updatePlayer)) {
                statement.setDouble(1, ePlayer.getExperience());

                statement.setInt(2, ePlayer.getMobKills());
                statement.setInt(3, ePlayer.getPlayerKills());
                statement.setInt(4, ePlayer.getDeaths());
                statement.setInt(5, ePlayer.getKillstreak());
                statement.setInt(6, ePlayer.getBestKillstreak());

                statement.setString(7, ePlayer.getUniqueId().toString());
                statement.executeUpdate();

                updater.sendPlayerUpdate(ePlayer.getUniqueId());
            }
        }));
    }

    public void createPlayer(EPlayer ePlayer) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {

            String createPlayer = "INSERT INTO " + this.getTablePrefix() + "players (uuid, experience, mob_kills, player_kills, deaths, killstreak, best_killstreak) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createPlayer)) {
                statement.setString(1, ePlayer.getUniqueId().toString());

                statement.setDouble(2, ePlayer.getExperience());

                statement.setInt(3, ePlayer.getMobKills());
                statement.setInt(4, ePlayer.getPlayerKills());
                statement.setInt(5, ePlayer.getDeaths());
                statement.setInt(6, ePlayer.getKillstreak());
                statement.setInt(7, ePlayer.getBestKillstreak());
                statement.executeUpdate();

                updater.sendPlayerUpdate(ePlayer.getUniqueId());
            }
        }), "create");
    }

    public void deletePlayer(EPlayer ePlayer) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deletePlayer = "DELETE FROM " + this.getTablePrefix() + "players WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(deletePlayer)) {
                statement.setString(1, ePlayer.getUniqueId().toString());
                statement.executeUpdate();
            }
        }));
    }

    public void selectPlayer(UUID uuid, Consumer<EPlayer> callback) {
        this.databaseConnector.connect(connection -> {
            String selectPlayers = "SELECT * FROM " + this.getTablePrefix() + "players where uuid = ?";

            try (PreparedStatement statement = connection.prepareStatement(selectPlayers)) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    UUID id = UUID.fromString(result.getString("uuid"));

                    double experience = result.getDouble("experience");

                    int mobKills = result.getInt("mob_kills");
                    int playerKills = result.getInt("player_kills");
                    int deaths = result.getInt("deaths");
                    int killstreak = result.getInt("killstreak");
                    int bestKillstreak = result.getInt("best_killstreak");

                    EPlayer ePlayer = new EPlayer(id, experience, mobKills, playerKills, deaths, killstreak, bestKillstreak);
                    callback.accept(ePlayer);
                }
            }
        });
    }

    public void getPlayers(Consumer<Map<UUID, EPlayer>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String selectPlayers = "SELECT * FROM " + this.getTablePrefix() + "players";

            Map<UUID, EPlayer> players = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectPlayers);
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));

                    double experience = result.getDouble("experience");

                    int mobKills = result.getInt("mob_kills");
                    int playerKills = result.getInt("player_kills");
                    int deaths = result.getInt("deaths");
                    int killstreak = result.getInt("killstreak");
                    int bestKillstreak = result.getInt("best_killstreak");

                    EPlayer ePlayer = new EPlayer(uuid, experience, mobKills,
                            playerKills, deaths, killstreak, bestKillstreak);
                    players.put(uuid, ePlayer);
                }
            }

            this.sync(() -> callback.accept(players));
        }));
    }

    public void getPlayer(Player player, Consumer<EPlayer> callback) {
        getPlayer(player.getUniqueId(), callback);
    }

    public void getPlayer(UUID uuid, Consumer<EPlayer> callback) {
        this.async(() -> selectPlayer(uuid, callback));
    }

    public void getPlayerOrCreate(Player player, Consumer<EPlayer> callback) {
        getPlayerOrCreate(player.getUniqueId(), callback);
    }

    public void getPlayerOrCreate(UUID uuid, Consumer<EPlayer> callback) {
        this.async(() -> {
            EPlayer[] array = new EPlayer[1];
            selectPlayer(uuid, data -> array[0] = data);

            EPlayer ePlayer = array[0];

            if (ePlayer == null) {
                ePlayer = new EPlayer(uuid);
                createPlayer(ePlayer);
            }

            callback.accept(ePlayer);
        });
    }

    public void getBoosts(Consumer<Map<UUID, Boost>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String selectBoosts = "SELECT * FROM " + this.getTablePrefix() + "boosts";

            Map<UUID, Boost> boosts = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectBoosts);
                while (result.next()) {
                    int id = result.getInt("id");

                    String uuidStr = result.getString("uuid");
                    UUID uuid = uuidStr == null ? null : UUID.fromString(uuidStr);

                    long expiration = result.getLong("expiration");
                    double multiplier = result.getInt("multiplier");

                    boosts.put(uuid, new Boost(id, expiration, multiplier));
                }
            }

            this.sync(() -> callback.accept(boosts));
        }));
    }

    public void deleteBoost(Boost boost) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosts WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteBoost)) {
                statement.setInt(1, boost.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void createBoost(UUID uuid, Boost boost) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createBoost = "INSERT INTO " + this.getTablePrefix() + "boosts (uuid, expiration, multiplier) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createBoost)) {
                statement.setString(1, uuid == null ? null : uuid.toString());

                statement.setLong(2, boost.getExpiration());
                statement.setDouble(3, boost.getMultiplier());
                statement.executeUpdate();
            }

            int boostId = this.lastInsertedId(connection, "boosts");

            this.sync(() -> boost.setId(boostId));
        }));
    }

    public void updateBoost(Boost boost) {
        this.databaseConnector.connect(connection -> {
            String updateBoost = "UPDATE " + this.getTablePrefix() + "boosts SET expiration = ?, multiplier = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateBoost)) {
                statement.setLong(1, boost.getExpiration());

                statement.setDouble(2, boost.getMultiplier());
                statement.setInt(3, boost.getId());

                statement.executeUpdate();
            }
        });
    }

    public void bulkUpdateBoosts(Collection<Boost> boosts) {
        this.databaseConnector.connect(connection -> {
            String updateBoost = "UPDATE " + this.getTablePrefix() + "boosts SET expiration = ?, multiplier = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateBoost)) {
                for (Boost boost : boosts) {
                    statement.setLong(1, boost.getExpiration());

                    statement.setDouble(2, boost.getMultiplier());
                    statement.setInt(3, boost.getId());

                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });
    }
}
