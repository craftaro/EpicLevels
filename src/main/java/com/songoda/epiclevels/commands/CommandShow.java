package com.songoda.epiclevels.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.gui.GUILevels;
import com.songoda.epiclevels.players.EPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class CommandShow extends AbstractCommand {
    private final EpicLevels instance;

    public CommandShow(EpicLevels instance) {
        super(CommandType.PLAYER_ONLY, "show");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) return ReturnType.SYNTAX_ERROR;

        List<EPlayer> players = this.instance.getPlayerManager().getPlayersUnsorted();

        Optional<EPlayer> targetOptional = players.stream()
                .filter(ePlayer -> ePlayer.getPlayer().getName() != null
                        && ePlayer.getPlayer().getName().toLowerCase().contains(args[0].toLowerCase()))
                .findAny();

        if (!targetOptional.isPresent()) {
            this.instance.getLocale().getMessage("gui.levels.noresults").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        this.instance.getGuiManager().showGUI((Player) sender, new GUILevels((Player) sender, targetOptional.get()));

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
