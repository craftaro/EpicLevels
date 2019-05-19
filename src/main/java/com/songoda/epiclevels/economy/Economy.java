package com.songoda.epiclevels.economy;

import org.bukkit.entity.Player;

public interface Economy {

    boolean AddToBalance(Player player, double amount);

    boolean hasBalance(Player player, double cost);

    boolean withdrawBalance(Player player, double cost);
}
