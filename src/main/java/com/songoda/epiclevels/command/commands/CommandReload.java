package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    public CommandReload(AbstractCommand parent) {
        super(parent, false, "reload");
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        instance.reload();
        sender.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + "&7Configuration and Language files reloaded."));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.admin";
    }

    @Override
    public String getSyntax() {
        return "/levels Reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
