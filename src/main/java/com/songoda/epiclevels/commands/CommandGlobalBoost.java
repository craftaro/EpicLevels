package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TimeUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandGlobalBoost extends AbstractCommand {

    private final EpicLevels instance;

    public CommandGlobalBoost(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "GlobalBoost");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 2) return ReturnType.SYNTAX_ERROR;

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[0]);
        } catch (Exception e) {
            instance.getLocale().getMessage("command.general.notint")
                    .processPlaceholder("number", args[1])
                    .sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0;
        for (String line : args) {
            long time = TimeUtils.parseTime(line);
            duration += time;
        }

        Boost boost = new Boost(duration + System.currentTimeMillis(), multiplier);
        instance.getBoostManager().setGlobalBoost(boost);
        instance.getDataManager().createBoost(null, boost);
        instance.getDataManager().getUpdater().sendBoostCreate(null, duration, multiplier, sender.getName());

        instance.getLocale().getMessage("event.boost.globalsuccess")
                .processPlaceholder("multiplier", multiplier)
                .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                .sendPrefixedMessage(sender);

        for (Player pl : Bukkit.getOnlinePlayers().stream().filter(p -> p != sender).collect(Collectors.toList()))
            instance.getLocale().getMessage("event.boost.globalannounce")
                    .processPlaceholder("player", sender.getName())
                    .processPlaceholder("multiplier", multiplier)
                    .processPlaceholder("duration", TimeUtils.makeReadable(duration))
                    .sendPrefixedMessage(pl);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.boost";
    }

    @Override
    public String getSyntax() {
        return "/levels GlobalBoost <Multiplier> <0h 20m>";
    }

    @Override
    public String getDescription() {
        return "Boost the server for a limited amount of time.";
    }
}
