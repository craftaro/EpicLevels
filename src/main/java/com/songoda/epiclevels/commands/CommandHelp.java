package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TextUtils;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandHelp extends AbstractCommand {
    private final EpicLevels instance;

    public CommandHelp(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "help");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        this.instance.getLocale().newMessage("&7Version " + this.instance.getDescription().getVersion()
                + " Created with <3 by &5&l&oSongoda").sendPrefixedMessage(sender);
        sender.sendMessage("");
        sender.sendMessage(TextUtils.formatText("&7Welcome to EpicLevels! To get started try using the /levels command to access the leaderboard."));
        sender.sendMessage("");
        sender.sendMessage(TextUtils.formatText("&6Commands:"));
        for (AbstractCommand command : this.instance.getCommandManager().getAllCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(TextUtils.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/levels help";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}
