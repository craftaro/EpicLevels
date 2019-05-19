package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRemoveGlobalBoost extends AbstractCommand {

    public CommandRemoveGlobalBoost(AbstractCommand parent) {
        super(parent, false, "GlobalBoost");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        instance.getBoostManager().clearGlobalBoost();

        sender.sendMessage("ALL GOOD HOMMIE");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.boost";
    }

    @Override
    public String getSyntax() {
        return "/levels RemoveGlobalBoost";
    }

    @Override
    public String getDescription() {
        return "Remove the boost from the server.";
    }
}
