package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
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

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!instance.getPlayerManager().containsPlayer(player.getUniqueId())) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[0])
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        Boost boost = instance.getBoostManager().removeBoost(player.getUniqueId());
        if (boost != null) {
            instance.getDataManager().deleteBoost(boost);
        }
        instance.getDataManager().getUpdater().sendBoostRemove(player.getUniqueId());
        instance.getLocale().getMessage("command.removeboost.success")
                .processPlaceholder("player", player.getName())
                .sendPrefixedMessage(sender);

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
