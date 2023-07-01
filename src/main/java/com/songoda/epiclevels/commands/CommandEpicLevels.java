package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.gui.GuiManager;
import com.songoda.epiclevels.gui.GUILevels;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandEpicLevels extends AbstractCommand {
    private final GuiManager guiManager;

    public CommandEpicLevels(GuiManager guiManager) {
        super(CommandType.PLAYER_ONLY, "EpicLevels");
        this.guiManager = guiManager;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.guiManager.showGUI((Player) sender, new GUILevels((Player) sender, null));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.menu";
    }

    @Override
    public String getSyntax() {
        return "/levels";
    }

    @Override
    public String getDescription() {
        return "Displays top levels.";
    }
}
