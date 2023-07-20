package com.craftaro.epiclevels.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.epiclevels.gui.GUILevels;
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
