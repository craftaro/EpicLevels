package com.songoda.epiclevels.economy;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.entity.Player;

public class VaultEconomy implements Economy {

    private final EpicLevels plugin;

    private final net.milkbowl.vault.economy.Economy vault;

    public VaultEconomy(EpicLevels plugin) {
        this.plugin = plugin;

        this.vault = plugin.getServer().getServicesManager().
                getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
    }

    @Override
    public boolean AddToBalance(Player player, double amount) {
        return vault.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean hasBalance(Player player, double cost) {
        return vault.has(player, cost);
    }

    @Override
    public boolean withdrawBalance(Player player, double cost) {
        return vault.withdrawPlayer(player, cost).transactionSuccess();
    }
}
