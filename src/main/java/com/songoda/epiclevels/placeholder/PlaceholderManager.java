package com.songoda.epiclevels.placeholder;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.Methods;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;

public class PlaceholderManager extends PlaceholderExpansion {

    private final EpicLevels plugin;

    private DecimalFormat decimalFormat = new DecimalFormat("###,###.###");

    public PlaceholderManager(EpicLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        EPlayer ePlayer = plugin.getPlayerManager().getPlayer(player);
        
        switch (identifier) {
            case "level":
                return decimalFormat.format(ePlayer.getLevel());
            case "experience":
                return decimalFormat.format(ePlayer.getExperience());
            case "kills":
                return decimalFormat.format(ePlayer.getKills());
            case "deaths":
                return decimalFormat.format(ePlayer.getDeaths());
            case "killstreak":
                return decimalFormat.format(ePlayer.getKillstreak());
            case "bestkillstreak":
                return decimalFormat.format(ePlayer.getBestKillstreak());
            case "kdr":
                return decimalFormat.format(ePlayer.getDeaths() == 0 ? ePlayer.getKills() : ePlayer.getKills() / ePlayer.getDeaths());
            case "nextlevel":
                return decimalFormat.format(ePlayer.getLevel() + 1);
            case "neededfornextlevel":
                return decimalFormat.format(EPlayer.experience(ePlayer.getLevel() + 1) - ePlayer.getExperience());
            case "boosterenabled":
                return plugin.getBoostManager().getBoost(ePlayer.getUniqueId()) == null
                        ? plugin.getLocale().getMessage("general.word.enabled")
                        : plugin.getLocale().getMessage("general.word.disabled");
            case "booster":
                if (plugin.getBoostManager().getBoost(ePlayer.getUniqueId()) == null) return "1";
                return decimalFormat.format(plugin.getBoostManager().getBoost(ePlayer.getUniqueId()).getMultiplier());
            case "globalboosterenabled":
                return plugin.getBoostManager().getGlobalBoost() == null
                        ? plugin.getLocale().getMessage("general.word.enabled")
                        : plugin.getLocale().getMessage("general.word.disabled");
            case "globalbooster":
                if (plugin.getBoostManager().getGlobalBoost() == null) return "1";
                return decimalFormat.format(plugin.getBoostManager().getGlobalBoost().getMultiplier());
            case "progressbar":
                long exp = ePlayer.getExperience() - EPlayer.experience(ePlayer.getLevel());
                double nextLevel = EPlayer.experience(ePlayer.getLevel() + 1) - EPlayer.experience(ePlayer.getLevel());
                return Methods.generateProgressBar(exp, nextLevel, true);
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
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

}
