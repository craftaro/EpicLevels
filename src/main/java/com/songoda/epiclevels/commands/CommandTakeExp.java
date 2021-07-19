package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandTakeExp extends AbstractCommand {

    private final EpicLevels instance;

    public CommandTakeExp(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "TakeExp");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!player.hasPlayedBefore()) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", args[0]).sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!Methods.isInt(args[1])) {
            instance.getLocale().getMessage("command.general.notint")
                    .processPlaceholder("number", args[1]).sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long amount = Long.parseLong(args[1]);

        EPlayer ePlayer = instance.getPlayerManager().getPlayer(player);
        ePlayer.addExperience(-amount);
        instance.getDataManager().updatePlayer(ePlayer);

        instance.getLocale().getMessage("command.removeexp.success")
                .processPlaceholder("amount", amount)
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
