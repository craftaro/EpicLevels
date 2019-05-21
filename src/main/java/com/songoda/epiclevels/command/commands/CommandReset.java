package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReset extends AbstractCommand {

    public CommandReset(AbstractCommand parent) {
        super(parent, false, "Reset");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        if (!player.hasPlayedBefore()) {
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.general.notonline", args[1]));
            return ReturnType.FAILURE;
        }

        instance.getPlayerManager().resetPlayer(player.getUniqueId());

        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.reset.success", player.getName()));

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.reset";
    }

    @Override
    public String getSyntax() {
        return "/levels Reset <Player>";
    }

    @Override
    public String getDescription() {
        return "Resets all stats for a player.";
    }
}
