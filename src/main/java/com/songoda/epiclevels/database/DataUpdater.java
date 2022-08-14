package com.songoda.epiclevels.database;

import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.locale.Message;
import com.songoda.core.utils.TimeUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataUpdater extends DataUpdaterAbstract {

    private final DataManager manager;

    private final List<UUID> toUpdate = new ArrayList<>();

    public DataUpdater(DataManager manager) {
        this.manager = manager;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), () -> getMessages(this::processMessage), 20, 20);
            Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), this::cleanMessages, 20, 600);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        toUpdate.clear();
    }

    public DataManager getManager() {
        return manager;
    }

    @Override
    public DatabaseConnector getConnector() {
        return manager.getDatabaseConnector();
    }

    @Override
    public String getTablePrefix() {
        return manager.getTablePrefix();
    }

    public String buildMessage(String id, Object... args) {
        String[] strings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            strings[i] = String.valueOf(args[i]);
        }
        return id + (args.length > 1 ? ":" + args.length : "") + "=" + String.join("_|||_", strings);
    }

    public void sendMessageAsync(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(manager.getPlugin(), () -> sendMessage(message));
    }

    public void sendMessageAsync(String message, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getPlugin(), () -> sendMessage(message), delay);
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
            args = new String[] {split[1]};
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
        if (toUpdate.contains(uuid)) {
            return;
        }

        toUpdate.add(uuid);
        Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getPlugin(), () -> {
            try {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    manager.selectPlayer(uuid, (data) -> {
                        if (data != null) {
                            EpicLevels.getInstance().getPlayerManager().addPlayer(data);
                        }
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            toUpdate.remove(uuid);
        }, 80);
    }

    public void processBoostCreate(UUID uuid, long duration, double multiplier, String sender) {
        Boost boost = new Boost(duration + System.currentTimeMillis(), multiplier);

        if (uuid == null) {
            // Global boost
            EpicLevels.getInstance().getBoostManager().setGlobalBoost(boost);
            Message message = EpicLevels.getInstance().getLocale().getMessage("event.boost.globalannounce")
                    .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                    .processPlaceholder("multiplier", multiplier)
                    .processPlaceholder("player", sender);

            Bukkit.getOnlinePlayers().forEach(message::sendPrefixedMessage);
        } else {
            // Player boost
            EpicLevels.getInstance().getBoostManager().addBoost(uuid, boost);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                EpicLevels.getInstance().getLocale().getMessage("event.boost.announce")
                        .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                        .processPlaceholder("multiplier", multiplier)
                        .processPlaceholder("player", sender)
                        .sendPrefixedMessage(player);
            }
        }
    }

    public void processBoostRemove(UUID uuid) {
        if (uuid == null) {
            EpicLevels.getInstance().getBoostManager().clearGlobalBoost();
        } else {
            EpicLevels.getInstance().getBoostManager().removeBoost(uuid);
        }
    }
}
