package com.songoda.epiclevels.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.boost.Boost;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRemoveGlobalBoost extends AbstractCommand {
    private final EpicLevels instance;

    public CommandRemoveGlobalBoost(EpicLevels instance) {
        super(CommandType.CONSOLE_OK, "RemoveGlobalBoost");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Boost boost = this.instance.getBoostManager().clearGlobalBoost();
        if (boost != null) {
            this.instance.getDataManager().deleteBoost(boost);
        }
        this.instance.getDataManager().getUpdater().sendBoostRemove(null);

        this.instance.getLocale().getMessage("command.removeglobalboost.success")
                .sendPrefixedMessage(sender);

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
        return "/levels RemoveGlobalBoost";
    }

    @Override
    public String getDescription() {
        return "Remove the boost from the server.";
    }
}
