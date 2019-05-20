package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {

    public CommandSettings(AbstractCommand parent) {
        super(parent, true, "settings");
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        Player player = (Player) sender;
        instance.getSettingsManager().openSettingsManager(player);
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
        return "/levels Settings";
    }

    @Override
    public String getDescription() {
        return "Edit EpicLevels Settings.";
    }
}
