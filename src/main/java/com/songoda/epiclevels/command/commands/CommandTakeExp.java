package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandTakeExp extends AbstractCommand {

    public CommandTakeExp(AbstractCommand parent) {
        super(parent, false, "TakeExp");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length != 3) return ReturnType.SYNTAX_ERROR;

        String playerStr = args[1].toLowerCase();
        long amount = Long.parseLong(args[2]);

        instance.getPlayerManager().getPlayer(Bukkit.getOfflinePlayer(playerStr)).addExperience(0L - amount);

        sender.sendMessage("ALL GOOD HOMMIE");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.takeexp";
    }

    @Override
    public String getSyntax() {
        return "/levels TakeExp <Player> <Amount>";
    }

    @Override
    public String getDescription() {
        return "Take experience from a player.";
    }
}
