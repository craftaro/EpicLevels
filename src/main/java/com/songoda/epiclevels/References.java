package com.songoda.epiclevels;

public class References {

    private String prefix;

    public References() {
        prefix = EpicLevels.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}
