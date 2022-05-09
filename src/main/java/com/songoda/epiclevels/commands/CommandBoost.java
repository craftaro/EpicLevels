package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TimeUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandBoost extends AbstractCommand {

    private final EpicLevels instance;

    public CommandBoost(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "Boost");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 3) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!instance.getPlayerManager().containsPlayer(player.getUniqueId())) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[0])
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[1]);
        } catch (Exception e) {
            instance.getLocale().getMessage("command.general.notint")
                    .processPlaceholder("number", args[1])
                    .sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0;
        for (int i = 1; i < args.length; i++) {
            String line = args[i];
            long time = TimeUtils.parseTime(line);
            duration += time;
        }

        Boost boost = new Boost(duration + System.currentTimeMillis(), multiplier);
        instance.getBoostManager().addBoost(player.getUniqueId(), boost);
        instance.getDataManager().createBoost(player.getUniqueId(), boost);
        instance.getDataManager().getUpdater().sendBoostCreate(player.getUniqueId(), duration, multiplier, sender.getName());

        instance.getLocale().getMessage("event.boost.success")
                .processPlaceholder("player", player.getName())
                .processPlaceholder("multiplier", multiplier)
                .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                .sendPrefixedMessage(sender);

        if (player.isOnline())
            instance.getLocale().getMessage("event.boost.announce")
                    .processPlaceholder("player", sender.getName())
                    .processPlaceholder("multiplier", multiplier)
                    .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                    .sendPrefixedMessage(player.getPlayer());

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.boost";
    }

    @Override
    public String getSyntax() {
        return "/levels Boost <player> <Multiplier> <0h 30m>";
    }

    @Override
    public String getDescription() {
        return "Boost a player for a limited amount of time.";
    }
}
