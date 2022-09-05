package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReset extends AbstractCommand {

    private final EpicLevels instance;

    public CommandReset(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "Reset");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("name", player.getName())
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        EPlayer ePlayer = instance.getPlayerManager().resetPlayer(player.getUniqueId());
        instance.getDataManager().deletePlayer(ePlayer);

        instance.getLocale().getMessage("command.reset.success")
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
