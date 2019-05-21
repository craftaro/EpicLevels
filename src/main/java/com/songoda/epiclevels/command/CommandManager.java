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

    private EpicLevels instance;
    private TabManager tabManager;

    private List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(EpicLevels instance) {
        this.instance = instance;
        this.tabManager = new TabManager(this);

        instance.getCommand("EpicLevels").setExecutor(this);

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
            instance.getCommand(abstractCommand.getCommand()).setTabCompleter(tabManager);
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
        commandSender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&7The commands you entered does not exist or is spelt incorrectly."));
        return true;
    }

    private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
        if (!(sender instanceof Player) && command.isNoConsole()) {
            sender.sendMessage("You must be a player to use this commands.");
            return;
        }
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            AbstractCommand.ReturnType returnType = command.runCommand(instance, sender, strings);
            if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
                sender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&cInvalid Syntax!"));
                sender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&7The valid syntax is: &6" + command.getSyntax() + "&7."));
            }
            return;
        }
        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
    }

    public List<AbstractCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
