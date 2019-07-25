package com.songoda.epiclevels.command;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.commands.*;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private EpicLevels plugin;
    private TabManager tabManager;

    private List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(EpicLevels plugin) {
        this.plugin = plugin;
        this.tabManager = new TabManager(this);

        plugin.getCommand("EpicLevels").setExecutor(this);

        AbstractCommand commandEpicLevels = addCommand(new CommandEpicLevels());

        addCommand(new CommandSettings(commandEpicLevels));
        addCommand(new CommandReload(commandEpicLevels));
        addCommand(new CommandShow(commandEpicLevels));
        addCommand(new CommandAddExp(commandEpicLevels));
        addCommand(new CommandTakeExp(commandEpicLevels));
        addCommand(new CommandHelp(commandEpicLevels));
        addCommand(new CommandBoost(commandEpicLevels));
        addCommand(new CommandRemoveBoost(commandEpicLevels));
        addCommand(new CommandGlobalBoost(commandEpicLevels));
        addCommand(new CommandRemoveGlobalBoost(commandEpicLevels));
        addCommand(new CommandReset(commandEpicLevels));

        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getParent() != null) continue;
            plugin.getCommand(abstractCommand.getCommand()).setTabCompleter(tabManager);
        }
    }

    private AbstractCommand addCommand(AbstractCommand abstractCommand) {
        commands.add(abstractCommand);
        return abstractCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getCommand() != null && abstractCommand.getCommand().equalsIgnoreCase(command.getName().toLowerCase())) {
                if (strings.length == 0 || abstractCommand.hasArgs()) {
                    processRequirements(abstractCommand, commandSender, strings);
                    return true;
                }
            } else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName())) {
                String cmd = strings[0];
                String cmd2 = strings.length >= 2 ? String.join(" ", strings[0], strings[1]) : null;
                for (String cmds : abstractCommand.getSubCommand()) {
                    if (cmd.equalsIgnoreCase(cmds) || (cmd2 != null && cmd2.equalsIgnoreCase(cmds))) {
                        processRequirements(abstractCommand, commandSender, strings);
                        return true;
                    }
                }
            }
        }
        plugin.getLocale().newMessage("&7The command you entered does not exist or is spelt incorrectly.").sendPrefixedMessage(commandSender);
        return true;
    }

    private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
        if (!(sender instanceof Player) && command.isNoConsole()) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            AbstractCommand.ReturnType returnType = command.runCommand(plugin, sender, strings);
            if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
                plugin.getLocale().newMessage("&cInvalid Syntax!").sendPrefixedMessage(sender);
                plugin.getLocale().newMessage("&7The valid syntax is: &6" + command.getSyntax() + "&7.").sendPrefixedMessage(sender);
            }
            return;
        }
        plugin.getLocale().newMessage("event.general.nopermission").sendPrefixedMessage(sender);
    }

    public List<AbstractCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
