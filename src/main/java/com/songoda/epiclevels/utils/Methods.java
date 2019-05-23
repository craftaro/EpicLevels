package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.utils.settings.Setting;
import com.songoda.epiclevels.utils.settings.SettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.TimeUnit;

public class Methods {

    public static ItemStack getGlass() {
        EpicLevels instance = EpicLevels.getInstance();
        return Methods.getGlass(instance.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), instance.getConfig().getInt("Interfaces.Glass Type 1"));
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        EpicLevels instance = EpicLevels.getInstance();
        if (type)
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 2"));
        else
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 3"));
    }

    private static ItemStack getGlass(Boolean rainbow, int type) {
        int randomNum = 1 + (int) (Math.random() * 6);
        ItemStack glass;
        if (rainbow) {
            glass = new ItemStack(EpicLevels.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) randomNum);
        } else {
            glass = new ItemStack(EpicLevels.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) type);
        }
        ItemMeta glassmeta = glass.getItemMeta();
        glassmeta.setDisplayName("§l");
        glass.setItemMeta(glassmeta);
        return glass;
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String formatTitle(String text) {
        if (text == null || text.equals(""))
            return "";
        if (!EpicLevels.getInstance().isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (text.length() > 31)
                text = text.substring(0, 29) + "...";
        }
        text = formatText(text);
        return text;
    }

    public static String makeReadable(Long time) {
        if (time == null)
            return "";
        return String.format("%dd %dh %dm",
                TimeUnit.MILLISECONDS.toDays(time),
                TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(time)),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)));
    }

    public static long parseTime(String input) {
        long result = 0;
        String number = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c) && !number.isEmpty()) {
                result += convert(Integer.parseInt(number), c);
                number = "";
            }
        }
        return result;
    }

    private static long convert(long value, char unit) {
        switch (unit) {
            case 'd':
                return value * 1000 * 60 * 60 * 24;
            case 'h':
                return value * 1000 * 60 * 60;
            case 'm':
                return value * 1000 * 60;
            case 's':
                return value * 1000;
        }
        return 0;
    }

    public static String generateProgressBar(double exp, double nextLevel, boolean placeholder) {
        double length = placeholder ? Setting.PROGRESS_BAR_LENGTH_PLACEHOLDER.getInt()
                : Setting.PROGRESS_BAR_LENGTH.getInt();
        double progress = (exp / nextLevel) * length;

        StringBuilder prog = new StringBuilder();
        for (int j = 0; j < length; j++)
            prog.append("&").append(j > progress ? "c" : "a").append("|");
        return prog.toString();
    }
}
