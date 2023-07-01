package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.settings.Settings;

import java.text.DecimalFormat;

public class Methods {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.###");

    public static String formatDecimal(double decimal) {
        return DECIMAL_FORMAT.format(decimal);
    }

    public static String generateProgressBar(double exp, double nextLevel, boolean placeholder) {
        double length = placeholder ?
                Settings.PROGRESS_BAR_LENGTH_PLACEHOLDER.getInt()
                : Settings.PROGRESS_BAR_LENGTH.getInt();
        double progress = (exp / nextLevel) * length;

        StringBuilder progressBar = new StringBuilder();
        for (int j = 0; j < length; ++j) {
            progressBar
                    .append("&")
                    .append(j > progress ? "c" : "a")
                    .append("|");
        }
        return progressBar.toString();
    }
}
