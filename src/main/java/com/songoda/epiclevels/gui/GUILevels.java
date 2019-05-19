package com.songoda.epiclevels.gui;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.AbstractChatConfirm;
import com.songoda.epiclevels.utils.Methods;
import com.songoda.epiclevels.utils.ServerVersion;
import com.songoda.epiclevels.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GUILevels extends AbstractGUI {

    private final EpicLevels plugin;
    private int position;

    private EPlayer target;

    private List<EPlayer> players;

    private Sorting sortingBy = Sorting.LEVELS;

    public GUILevels(EpicLevels plugin, Player player, EPlayer target) {
        super(player);
        plugin.getPlayerManager().getPlayer(player);
        this.plugin = plugin;
        this.players = plugin.getPlayerManager().getPlayers();
        sort();
        this.target = target;

        updateTitle();
    }

    private void updateTitle() {
        init("Showing Top " + sortingBy.getName(), 54);
        constructGUI();
    }

    private void sort() {
        this.position = target == null ? 0 : players.indexOf(target);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(4, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));

        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(18, Methods.getBackgroundGlass(false));
        inventory.setItem(26, Methods.getBackgroundGlass(false));

        inventory.setItem(27, Methods.getBackgroundGlass(false));
        inventory.setItem(35, Methods.getBackgroundGlass(false));

        inventory.setItem(36, Methods.getBackgroundGlass(true));
        inventory.setItem(44, Methods.getBackgroundGlass(true));

        inventory.setItem(45, Methods.getBackgroundGlass(true));
        inventory.setItem(46, Methods.getBackgroundGlass(true));
        inventory.setItem(47, Methods.getBackgroundGlass(false));
        inventory.setItem(48, Methods.getBackgroundGlass(false));
        inventory.setItem(49, Methods.getBackgroundGlass(false));
        inventory.setItem(50, Methods.getBackgroundGlass(false));
        inventory.setItem(51, Methods.getBackgroundGlass(false));
        inventory.setItem(52, Methods.getBackgroundGlass(true));
        inventory.setItem(53, Methods.getBackgroundGlass(true));


        createButton(2, Material.EXPERIENCE_BOTTLE, "&e&lTop Levels");
        createButton(3, Material.SKELETON_SKULL, "&a&lTop Mob Killers");
        createButton(5, Material.PLAYER_HEAD, "&c&lTop Player Killers");
        createButton(6, Material.DIAMOND_SWORD, "&b&lTop Killstreaks");
        createButton(16, Material.COMPASS, "&6&lSearch");

        createButton(13, Material.REDSTONE, "&7Sorting by: &6" + sortingBy.getName() + "&7.",
                "&8Click to go to you.");

        for (int i = 0; i < 7; i++) {
            int current = i + (position - 3 < 0 ? 0 : (position + 3 > (players.size() - 1) ? (players.size() - 7) : position - 3));
            if (current > (players.size() - 1)) break;
            EPlayer selected = players.get(current);

            OfflinePlayer targetPlayer = selected.getPlayer();

            ItemStack head = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
            SkullMeta meta = ((SkullMeta) head.getItemMeta());
            if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                meta.setOwningPlayer(targetPlayer);
            else
                meta.setOwner(targetPlayer.getName());
            head.setItemMeta(meta);

            long exp = selected.getExperience() - EPlayer.experience(selected.getLevel());

            double nextLevel = EPlayer.experience(selected.getLevel() + 1) - EPlayer.experience(selected.getLevel());

            double length = 36;
            double progress = (exp / nextLevel) * length;

            StringBuilder prog = new StringBuilder();
            for (int j = 0; j < length; j++)
                prog.append("&").append(j > progress ? "c" : "a").append("|");

            int slot = 37 + i;

            if (current == position)
                createButton(slot + 9, Material.ARROW, "&6Selected");

            createButton(slot, head, (current + 1) + " &6&l" + targetPlayer.getName(),
                    "&7Level " + (selected.getLevel()),
                    "&7EXP: &6" + selected.getExperience() + "&7/&6" + EPlayer.experience(selected.getLevel() + 1),
                    prog.toString());

            if (current == position)
                createButton(slot - 9, Material.DIAMOND_SWORD, "&6&lStats",
                        "&7Total Kills: &6" + selected.getKills(),
                        "&7Player Kills: &6" + selected.getPlayerKills(),
                        "&7Mob Kills: &6" + selected.getMobKills(),
                        "&7Deaths: &6" + selected.getDeaths(),
                        "&7KDR: &6" + (selected.getDeaths() == 0 ? selected.getPlayerKills() : ((double)selected.getPlayerKills()) / selected.getDeaths()),
                        "&7Current Killstreak: &6" + selected.getKillstreak(),
                        "&7Best Killstreak: &6" + selected.getBestKillstreak());

            registerClickable(slot, ((player1, inventory1, cursor, slot1, type) -> {
                position = current;
                updateTitle();
            }));

        }
    }


    @Override
    protected void registerClickables() {

        registerClickable(2, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers();
            position = players.indexOf(target);
            sort();
            sortingBy = Sorting.LEVELS;
            updateTitle();
        }));

        registerClickable(3, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers().stream()
                    .sorted(Comparator.comparingInt(EPlayer::getMobKills)).collect(Collectors.toList());
            Collections.reverse(players);
            sort();
            sortingBy = Sorting.MOB_KILLS;
            updateTitle();
        }));

        registerClickable(5, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers().stream()
                    .sorted(Comparator.comparingInt(EPlayer::getPlayerKills)).collect(Collectors.toList());
            Collections.reverse(players);
            sort();
            sortingBy = Sorting.PLAYER_KILLS;
            updateTitle();
        }));

        registerClickable(6, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers().stream()
                    .sorted(Comparator.comparingInt(EPlayer::getKillstreak)).collect(Collectors.toList());
            Collections.reverse(players);
            sort();
            sortingBy = Sorting.KILL_STREAKS;
            updateTitle();
        }));

        registerClickable(13, ((player1, inventory1, cursor, slot, type) -> {
            this.position = players.indexOf(plugin.getPlayerManager().getPlayer(player));
            updateTitle();
        }));

        registerClickable(16, ((player1, inventory1, cursor, slot, type) -> {
            player.sendMessage("Enter name to search...");
            AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event -> {
                Optional<EPlayer> targetOptional = players.stream()
                        .filter(ePlayer -> ePlayer.getPlayer().getName() != null
                                && ePlayer.getPlayer().getName().toLowerCase().contains(event.getMessage().toLowerCase()))
                        .limit(1).findAny();

                if (!targetOptional.isPresent()) {
                    player.sendMessage("No results found...");
                    return;
                }
                target = targetOptional.get();
                sort();
            });
            abstractChatConfirm.setOnClose(this::updateTitle);
        }));

    }

    @Override
    protected void registerOnCloses() {

    }

    public enum Sorting {

        LEVELS("Levels"),
        MOB_KILLS("Mob Kills"),
        PLAYER_KILLS("Player Kills"),
        KILL_STREAKS("Killstreaks");

        private final String name;

        Sorting(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
