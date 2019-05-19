package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandGlobalBoost extends AbstractCommand {

    public CommandGlobalBoost(AbstractCommand parent) {
        super(parent, false, "GlobalBoost");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length < 3) return ReturnType.SYNTAX_ERROR;

        int multiplier = Integer.parseInt(args[1]);

        long duration = 0;
        for (int i = 1; i < args.length; i++) {
            String line = args[i];
            long time = Methods.parseTime(line);
                duration += time;
        }

        instance.getBoostManager().setGlobalBoost(new Boost(duration + System.currentTimeMillis(), multiplier));

        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.boost.globalsuccess", multiplier, Methods.makeReadable(duration)));

        for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != sender).collect(Collectors.toList()))
            pl.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.boost.globalannounce", sender.getName(), multiplier, Methods.makeReadable(duration)));

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
        return "/levels GlobalBoost <Multiplier> <1h 30m>";
    }

    @Override
    public String getDescription() {
        return "Boost the server for a limited amount of time.";
    }
}
