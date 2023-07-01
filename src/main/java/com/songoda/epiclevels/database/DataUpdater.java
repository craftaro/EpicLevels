package com.songoda.epiclevels.database;

import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.locale.Message;
import com.craftaro.core.utils.TimeUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataUpdater extends DataUpdaterAbstract {
    private final DataManager manager;
    private final EpicLevels plugin;

    private final List<UUID> toUpdate = new ArrayList<>();

    public DataUpdater(DataManager manager, EpicLevels plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this.manager.getPlugin(), () -> getMessages(this::processMessage), 20, 20);
            Bukkit.getScheduler().runTaskTimerAsynchronously(this.manager.getPlugin(), this::cleanMessages, 20, 600);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.toUpdate.clear();
    }

    public DataManager getManager() {
        return this.manager;
    }

    @Override
    public DatabaseConnector getConnector() {
        return this.manager.getDatabaseConnector();
    }

    @Override
    public String getTablePrefix() {
        return this.manager.getTablePrefix();
    }

    public String buildMessage(String id, Object... args) {
        String[] strings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            strings[i] = String.valueOf(args[i]);
        }
        return id + (args.length > 1 ? ":" + args.length : "") + "=" + String.join("_|||_", strings);
    }

    public void sendMessageAsync(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(this.manager.getPlugin(), () -> sendMessage(message));
    }

    public void sendMessageAsync(String message, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.manager.getPlugin(), () -> sendMessage(message), delay);
    }

    public void sendPlayerUpdate(UUID uuid) {
        sendMessageAsync(buildMessage("PLAYERUPDATE", uuid), 80);
    }

    public void sendBoostCreate(UUID uuid, long duration, double multiplier, String sender) {
        sendMessageAsync(buildMessage("BOOSTCREATE", uuid, duration, multiplier, sender));
    }

    public void sendBoostRemove(UUID uuid) {
        sendMessageAsync(buildMessage("BOOSTREMOVE", uuid));
    }

    public void processMessage(String msg) {
        String[] split = msg.split("=", 2);
        if (split.length < 2) {
            return;
        }
        String[] type = split[0].split(":");
        if (type.length < 1) {
            return;
        }
        String id = type[0];
        int length = type.length > 1 ? Integer.parseInt(type[1]) : 1;

        String[] args;
        if (length <= 1) {
            args = new String[]{split[1]};
        } else {
            args = split[1].split("_\\|\\|\\|_", length);
            if (args.length != length) {
                return;
            }
        }
        try {
            processUpdate(id, args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void processUpdate(String id, String[] args) {
        switch (id.toUpperCase()) {
            case "PLAYERUPDATE":
                processPlayerUpdate(UUID.fromString(args[0]));
                break;
            case "BOOSTCREATE":
                processBoostCreate(
                        args[0].equalsIgnoreCase("null") ? null : UUID.fromString(args[0]),
                        Long.parseLong(args[1]),
                        Double.parseDouble(args[2]),
                        args[3]
                );
                break;
            case "BOOSTREMOVE":
                processBoostRemove(args[0].equalsIgnoreCase("null") ? null : UUID.fromString(args[0]));
                break;
            default:
                throw new IllegalArgumentException("Cannot process update with the ID: " + id);
        }
    }

    public void processPlayerUpdate(UUID uuid) {
        if (this.toUpdate.contains(uuid)) {
            return;
        }

        this.toUpdate.add(uuid);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.manager.getPlugin(), () -> {
            try {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    this.manager.selectPlayer(uuid, (data) -> {
                        if (data != null) {
                            this.plugin.getPlayerManager().addPlayer(data);
                        }
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            this.toUpdate.remove(uuid);
        }, 80);
    }

    public void processBoostCreate(UUID uuid, long duration, double multiplier, String sender) {
        Boost boost = new Boost(duration + System.currentTimeMillis(), multiplier);

        if (uuid == null) {
            // Global boost
            this.plugin.getBoostManager().setGlobalBoost(boost);
            Message message = this.plugin.getLocale().getMessage("event.boost.globalannounce")
                    .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                    .processPlaceholder("multiplier", multiplier)
                    .processPlaceholder("player", sender);

            Bukkit.getOnlinePlayers().forEach(message::sendPrefixedMessage);
        } else {
            // Player boost
            this.plugin.getBoostManager().addBoost(uuid, boost);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                this.plugin.getLocale().getMessage("event.boost.announce")
                        .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                        .processPlaceholder("multiplier", multiplier)
                        .processPlaceholder("player", sender)
                        .sendPrefixedMessage(player);
            }
        }
    }

    public void processBoostRemove(UUID uuid) {
        if (uuid == null) {
            this.plugin.getBoostManager().clearGlobalBoost();
        } else {
            this.plugin.getBoostManager().removeBoost(uuid);
        }
    }
}
