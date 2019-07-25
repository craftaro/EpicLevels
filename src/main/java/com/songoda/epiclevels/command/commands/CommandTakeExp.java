package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandTakeExp extends AbstractCommand {

    public CommandTakeExp(AbstractCommand parent) {
        super(parent, false, "TakeExp");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length != 3) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        if (!player.hasPlayedBefore()) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[1]).sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!Methods.isInt(args[2])) {
            instance.getLocale().getMessage("command.general.notint")
                    .processPlaceholder("number", args[2]).sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long amount = Long.parseLong(args[2]);

        instance.getPlayerManager().getPlayer(player).addExperience(0L - amount);

        instance.getLocale().getMessage("command.removeexp.success")
                .processPlaceholder("amount", amount)
                .processPlaceholder("player", player.getName())
                .sendPrefixedMessage(sender);

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
