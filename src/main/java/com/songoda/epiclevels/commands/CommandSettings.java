package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.PluginConfigGui;
import com.songoda.core.gui.GuiManager;
import com.songoda.epiclevels.EpicLevels;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {

    EpicLevels instance;
    GuiManager guiManager;

    public CommandSettings(GuiManager guiManager) {
        super(CommandType.PLAYER_ONLY, "settings");
        this.guiManager = guiManager;
        instance = EpicLevels.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        guiManager.showGUI((Player) sender, new PluginConfigGui(instance));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
