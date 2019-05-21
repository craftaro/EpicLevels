package com.songoda.epiclevels.placeholder;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceholderManager extends PlaceholderExpansion {

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        EPlayer ePlayer = EpicLevels.getInstance().getPlayerManager().getPlayer(player);

        switch (identifier) {
            case "level":
                return String.valueOf(ePlayer.getLevel());
            case "experience":
                return String.valueOf(ePlayer.getExperience());
            case "kills":
                return String.valueOf(ePlayer.getKills());
            case "deaths":
                return String.valueOf(ePlayer.getDeaths());
            case "killstreak":
                return String.valueOf(ePlayer.getKillstreak());
            case "bestkillstreak":
                return String.valueOf(ePlayer.getBestKillstreak());
            case "kdr":
                return String.valueOf(ePlayer.getKills() / ePlayer.getDeaths());
            case "nextlevel":
                return String.valueOf(ePlayer.getLevel() + 1);
            case "neededfornextlevel":
                return String.valueOf(EPlayer.experience(ePlayer.getLevel() + 1) - ePlayer.getExperience());
            default:
                return null;
        }
    }

    @Override
    public String getIdentifier() {
        return "epiclevels";
    }

    @Override
    public String getAuthor() {
        return "Songoda";
    }

    @Override
    public String getVersion() {
        return EpicLevels.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

}
