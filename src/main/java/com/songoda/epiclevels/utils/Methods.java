package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.settings.Settings;

import java.text.DecimalFormat;

public class Methods {

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

    public static String formatDecimal(double decimal) {
        return new DecimalFormat("###,###.###").format(decimal);
    }

    public static String generateProgressBar(double exp, double nextLevel, boolean placeholder) {
        double length = placeholder ? Settings.PROGRESS_BAR_LENGTH_PLACEHOLDER.getInt()
                : Settings.PROGRESS_BAR_LENGTH.getInt();
        double progress = (exp / nextLevel) * length;

        StringBuilder prog = new StringBuilder();
        for (int j = 0; j < length; j++)
            prog.append("&").append(j > progress ? "c" : "a").append("|");
        return prog.toString();
    }
}
