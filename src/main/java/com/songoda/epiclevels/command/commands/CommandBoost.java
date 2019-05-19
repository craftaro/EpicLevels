package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandBoost extends AbstractCommand {

    public CommandBoost(AbstractCommand parent) {
        super(parent, false, "Boost");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length < 4) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1].toLowerCase());
        int multiplier = Integer.parseInt(args[2]);

        long duration = 0;
        for (int i = 2; i < args.length; i++) {
            String line = args[i];
            long time = Methods.parseTime(line);
                duration += time;
        }

        instance.getBoostManager().addBoost(player.getUniqueId(), new Boost(duration + System.currentTimeMillis(), multiplier));

        sender.sendMessage("ALL GOOD HOMMIE " + duration);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.boost";
    }

    @Override
    public String getSyntax() {
        return "/levels Boost <player> <Multiplier> <1h 30m>";
    }

    @Override
    public String getDescription() {
        return "Boost a player for a limited amount of time.";
    }
}
