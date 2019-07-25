package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRemoveBoost extends AbstractCommand {

    public CommandRemoveBoost(AbstractCommand parent) {
        super(parent, false, "RemoveBoost");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getPlayer(args[1].toLowerCase());

        if (player == null) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[1])
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        instance.getBoostManager().removeBoost(player.getUniqueId());

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
        return "/levels RemoveBoost <player>";
    }

    @Override
    public String getDescription() {
        return "Remove a boost from a player";
    }
}
