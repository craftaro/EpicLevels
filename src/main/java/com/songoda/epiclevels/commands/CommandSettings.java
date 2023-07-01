package com.songoda.epiclevels.commands;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.PluginConfigGui;
import com.songoda.core.gui.GuiManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {
    private final SongodaPlugin plugin;
    private final GuiManager guiManager;

    public CommandSettings(GuiManager guiManager, SongodaPlugin plugin) {
        super(CommandType.PLAYER_ONLY, "settings");
        this.guiManager = guiManager;
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.guiManager.showGUI((Player) sender, new PluginConfigGui(this.plugin));
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
