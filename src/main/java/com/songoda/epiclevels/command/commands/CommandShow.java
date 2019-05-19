package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.gui.GUILevels;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandShow extends AbstractCommand {

    public CommandShow(AbstractCommand parent) {
        super(parent, true, "show");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {

        new GUILevels(instance, (Player)sender, instance.getPlayerManager().getPlayer(Bukkit.getOfflinePlayer(args[1])));

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.show";
    }

    @Override
    public String getSyntax() {
        return "/levels show <player>";
    }

    @Override
    public String getDescription() {
        return "Lookup a show.";
    }
}
