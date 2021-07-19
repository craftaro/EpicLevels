package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRemoveBoost extends AbstractCommand {

    private final EpicLevels instance;

    public CommandRemoveBoost(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "RemoveBoost");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getPlayer(args[0].toLowerCase());

        if (player == null) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[0])
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        instance.getBoostManager().removeBoost(player.getUniqueId());

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
        return "/levels RemoveBoost <player>";
    }

    @Override
    public String getDescription() {
        return "Remove a boost from a player";
    }
}
