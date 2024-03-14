package com.craftaro.epiclevels.database;

import com.craftaro.core.database.MySQLConnector;
import com.craftaro.epiclevels.EpicLevels;
import com.craftaro.epiclevels.boost.Boost;
import com.craftaro.epiclevels.players.EPlayer;
import com.craftaro.third_party.org.jooq.Record;
import com.craftaro.third_party.org.jooq.*;
import com.craftaro.third_party.org.jooq.impl.DSL;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class DataHelper {
    private final EpicLevels plugin;

    private final DataUpdater updater;

    public DataHelper(EpicLevels plugin) {
        this.plugin = plugin;

        this.updater = new DataUpdater(this, this.plugin);
        if (this.plugin.getDatabaseConnector() instanceof MySQLConnector) {
            this.updater.onEnable();
        }
    }

    public DataUpdater getUpdater() {
        return this.updater;
    }

    public void bulkUpdatePlayers(Collection<EPlayer> ePlayers) {
        this.plugin.getDatabaseConnector().connectDSL(context -> {
            List<Query> queries = new ArrayList<>();
            for (EPlayer ePlayer : ePlayers) {
                if (ePlayer.isSaved()) {
                    continue;
                }
                queries.add(
                        DSL.update(DSL.table(DSL.name(this.getTablePrefix() + "players")))

                                .set(DSL.field("experience"), ePlayer.getExperience())
                                .set(DSL.field("mob_kills"), ePlayer.getMobKills())
                                .set(DSL.field("player_kills"), ePlayer.getPlayerKills())
                                .set(DSL.field("deaths"), ePlayer.getDeaths())
                                .set(DSL.field("killstreak"), ePlayer.getKillStreak())
                                .set(DSL.field("best_killstreak"), ePlayer.getBestKillStreak())

                                .where(DSL.field("uuid").eq(ePlayer.getUniqueId().toString()))
                );
            }
            context.batch(queries).execute();
        });
    }

    public void updatePlayer(EPlayer ePlayer) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                context.update(DSL.table(this.getTablePrefix() + "players"))
                        .set(DSL.field("experience"), ePlayer.getExperience())
                        .set(DSL.field("mob_kills"), ePlayer.getMobKills())
                        .set(DSL.field("player_kills"), ePlayer.getPlayerKills())
                        .set(DSL.field("deaths"), ePlayer.getDeaths())
                        .set(DSL.field("killstreak"), ePlayer.getKillStreak())
                        .set(DSL.field("best_killstreak"), ePlayer.getBestKillStreak())
                        .where(DSL.field("uuid").eq(ePlayer.getUniqueId().toString()))
                        .execute();
            });

            this.updater.sendPlayerUpdate(ePlayer.getUniqueId());
        });
    }

    public void createPlayer(EPlayer ePlayer) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                context.insertInto(DSL.table(this.getTablePrefix() + "players"))
                        .columns(DSL.field("uuid"),
                                DSL.field("experience"),
                                DSL.field("mob_kills"),
                                DSL.field("player_kills"),
                                DSL.field("deaths"),
                                DSL.field("killstreak"),
                                DSL.field("best_killstreak"))
                        .values(ePlayer.getUniqueId().toString(),
                                ePlayer.getExperience(),
                                ePlayer.getMobKills(),
                                ePlayer.getPlayerKills(),
                                ePlayer.getDeaths(),
                                ePlayer.getKillStreak(),
                                ePlayer.getBestKillStreak())
                        .execute();
            });

            this.updater.sendPlayerUpdate(ePlayer.getUniqueId());
        });
    }

    public void deletePlayer(EPlayer ePlayer) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                context.delete(DSL.table(this.getTablePrefix() + "players"))
                        .where(DSL.field("uuid").eq(ePlayer.getUniqueId().toString()))
                        .execute();
            });
            ePlayer.setSaved(true);
        });
    }

    public void selectPlayer(UUID uuid, Consumer<EPlayer> callback) {
        this.plugin.getDatabaseConnector().connectDSL(context -> {
            try (SelectSelectStep<Record> selectQuery = context.select()) {
                Result<Record> result = selectQuery
                        .from(DSL.table(this.getTablePrefix() + "players"))
                        .where(DSL.field("uuid").eq(uuid.toString()))
                        .fetch();

                if (result.isEmpty()) {
                    callback.accept(null);
                } else {
                    Record record = result.get(0);

                    UUID id = UUID.fromString(record.get("uuid", String.class));

                    double experience = record.get("experience", double.class);

                    int mobKills = record.get("mob_kills", int.class);
                    int playerKills = record.get("player_kills", int.class);
                    int deaths = record.get("deaths", int.class);
                    int killStreak = record.get("killstreak", int.class);
                    int bestKillStreak = record.get("best_killstreak", int.class);

                    callback.accept(new EPlayer(id, experience, mobKills, playerKills, deaths, killStreak, bestKillStreak));
                }
            }
        });
    }

    public void getPlayers(Consumer<Map<UUID, EPlayer>> callback) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                Map<UUID, EPlayer> players = new HashMap<>();

                try (SelectSelectStep<Record> selectQuery = context.select()) {
                    Result<Record> result = selectQuery
                            .from(DSL.table(this.getTablePrefix() + "players"))
                            .fetch();

                    for (Record record : result) {
                        UUID id = UUID.fromString(record.get("uuid", String.class));

                        double experience = record.get("experience", double.class);

                        int mobKills = record.get("mob_kills", int.class);
                        int playerKills = record.get("player_kills", int.class);
                        int deaths = record.get("deaths", int.class);
                        int killStreak = record.get("killstreak", int.class);
                        int bestKillStreak = record.get("best_killstreak", int.class);

                        players.put(id, new EPlayer(id, experience, mobKills, playerKills, deaths, killStreak, bestKillStreak));
                    }
                }

                runSync(() -> callback.accept(players));
            });
        });
    }

    public void getPlayer(Player player, Consumer<EPlayer> callback) {
        getPlayer(player.getUniqueId(), callback);
    }

    public void getPlayer(UUID uuid, Consumer<EPlayer> callback) {
        runAsync(() -> {
            selectPlayer(uuid, (player) -> {
                if (player != null) {
                    callback.accept(player);
                }
            });
        });
    }

    public void getPlayerOrCreate(Player player, Consumer<EPlayer> callback) {
        getPlayerOrCreate(player.getUniqueId(), callback);
    }

    public void getPlayerOrCreate(UUID uuid, Consumer<EPlayer> callback) {
        runAsync(() -> {
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
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                Map<UUID, Boost> boosts = new HashMap<>();

                try (SelectSelectStep<Record> selectQuery = context.select()) {
                    Result<Record> result = selectQuery
                            .from(DSL.table(this.getTablePrefix() + "boosts"))
                            .fetch();

                    for (Record record : result) {
                        int id = record.get("id", int.class);

                        String uuidStr = record.get("uuid", String.class);
                        UUID uuid = uuidStr == null ? null : UUID.fromString(uuidStr);

                        long expiration = record.get("expiration", long.class);
                        double multiplier = record.get("multiplier", double.class);

                        boosts.put(uuid, new Boost(id, expiration, multiplier));
                    }
                }

                runSync(() -> callback.accept(boosts));
            });
        });
    }

    public void deleteBoost(Boost boost) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                try (DeleteUsingStep<Record> deleteQuery = context.delete(DSL.table(this.getTablePrefix() + "boosts"))) {
                    deleteQuery
                            .where(DSL.field("id").eq(boost.getId()))
                            .execute();
                }
            });
        });
    }

    public void createBoost(UUID uuid, Boost boost) {
        runAsync(() -> {
            this.plugin.getDatabaseConnector().connectDSL(context -> {
                Result<Record> insertedBoost = context.insertInto(DSL.table(this.getTablePrefix() + "boosts"))
                        .set(DSL.field("uuid"), uuid == null ? null : uuid.toString())
                        .set(DSL.field("expiration"), boost.getExpiration())
                        .set(DSL.field("multiplier"), boost.getMultiplier())
                        .returning(DSL.field("id"))
                        .fetch();

                int boostId = insertedBoost.get(0).get("id", int.class);
                runSync(() -> boost.setId(boostId));
            });
        });
    }

    public void updateBoost(Boost boost) {
        this.plugin.getDatabaseConnector().connectDSL(context -> {
            context.update(DSL.table(this.getTablePrefix() + "boosts"))
                    .set(DSL.field("expiration"), boost.getExpiration())
                    .set(DSL.field("multiplier"), boost.getMultiplier())
                    .where(DSL.field("id").eq(boost.getId()))
                    .execute();
        });
    }

    public void bulkUpdateBoosts(Collection<Boost> boosts) {
        this.plugin.getDatabaseConnector().connectDSL(context -> {
            List<Query> queries = new ArrayList<>();
            for (Boost boost : boosts) {
                queries.add(
                        context.update(DSL.table(this.getTablePrefix() + "boosts"))
                                .set(DSL.field("expiration"), boost.getExpiration())
                                .set(DSL.field("multiplier"), boost.getMultiplier())
                                .where(DSL.field("id").eq(boost.getId()))
                );
            }
            context.batch(queries).execute();
        });
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.plugin.getDescription().getName().toLowerCase() + '_';
    }

    private void runSync(Runnable runnable) {
        this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }

    private void runAsync(Runnable runnable) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }
}
