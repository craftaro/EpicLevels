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

import java.text.DecimalFormat;
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
        init(plugin.getLocale().getMessage("gui.levels.title")
                .processPlaceholder("type", sortingBy.getName()).getMessage(), 54);
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


        createButton(2, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.EXPERIENCE_BOTTLE : Material.valueOf("EXP_BOTTLE"), plugin.getLocale().getMessage("gui.levels.toplevels").getMessage());
        createButton(3, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SKELETON_SKULL : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 0), plugin.getLocale().getMessage("gui.levels.topmobs").getMessage());
        createButton(5, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3), plugin.getLocale().getMessage("gui.levels.topplayers").getMessage());
        createButton(6, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.levels.topkillstreaks").getMessage());
        createButton(16, Material.COMPASS, plugin.getLocale().getMessage("gui.levels.search").getMessage());

        createButton(13, Material.REDSTONE, plugin.getLocale().getMessage("gui.levels.sortingby").processPlaceholder("type", sortingBy.getName()).getMessage(),
                plugin.getLocale().getMessage("gui.levels.you").getMessage());

        for (int i = 0; i < 7; i++) {
            int current = i + (position - 3 < 0 ? 0 : (position + 3 > (players.size() - 1) ? (players.size() - 7) : position - 3));

            if (current < 0 || current > players.size() - 1) continue;
            EPlayer selected = players.get(current);
            if (selected.getPlayer() == null || selected.getPlayer().getName() == null)
                continue;

            OfflinePlayer targetPlayer = selected.getPlayer();

            ItemStack head = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
            SkullMeta meta = ((SkullMeta) head.getItemMeta());
            if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                meta.setOwningPlayer(targetPlayer);
            else
                meta.setOwner(targetPlayer.getName());
            head.setItemMeta(meta);

            double exp = selected.getExperience() - EPlayer.experience(selected.getLevel());

            double nextLevel = EPlayer.experience(selected.getLevel() + 1) - EPlayer.experience(selected.getLevel());


            String prog = Methods.generateProgressBar(exp, nextLevel, false);

            int slot = 37 + i;

            if (current == position)
                createButton(slot + 9, Material.ARROW, plugin.getLocale().getMessage("gui.levels.selected").getMessage());

            createButton(slot, head, plugin.getLocale().getMessage("gui.levels.name").processPlaceholder("position", current + 1).processPlaceholder("name", targetPlayer.getName()).getMessage(),
                    plugin.getLocale().getMessage("gui.levels.level").processPlaceholder("level", Methods.formatDecimal(selected.getLevel())).getMessage(),
                    plugin.getLocale().getMessage("gui.levels.exp").processPlaceholder("exp", Methods.formatDecimal(selected.getExperience())).processPlaceholder("expnext", Methods.formatDecimal(EPlayer.experience(selected.getLevel() + 1))).getMessage(),
                    prog);

            if (current == position)
                createButton(slot - 9, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.levels.stats").getMessage(),
                        plugin.getLocale().getMessage("gui.levels.totalkills").processPlaceholder("kills", Methods.formatDecimal(selected.getKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.playerkills").processPlaceholder("kills", Methods.formatDecimal(selected.getPlayerKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.mobkills").processPlaceholder("kills", Methods.formatDecimal(selected.getMobKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.deaths").processPlaceholder("deaths", Methods.formatDecimal(selected.getDeaths())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.kdr").processPlaceholder("kdr", Methods.formatDecimal(selected.getDeaths() == 0 ? selected.getPlayerKills() : ((double) selected.getPlayerKills()) / (double)selected.getDeaths())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.killstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getKillstreak())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.bestkillstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getBestKillstreak())).getMessage());

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
            sortingBy = Sorting.MOBKILLS;
            updateTitle();
        }));

        registerClickable(5, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers().stream()
                    .sorted(Comparator.comparingInt(EPlayer::getPlayerKills)).collect(Collectors.toList());
            Collections.reverse(players);
            sort();
            sortingBy = Sorting.PLAYERKILLS;
            updateTitle();
        }));

        registerClickable(6, ((player1, inventory1, cursor, slot, type) -> {
            players = plugin.getPlayerManager().getPlayers().stream()
                    .sorted(Comparator.comparingInt(EPlayer::getKillstreak)).collect(Collectors.toList());
            Collections.reverse(players);
            sort();
            sortingBy = Sorting.KILLSTREAKS;
            updateTitle();
        }));

        registerClickable(13, ((player1, inventory1, cursor, slot, type) -> {
            this.position = players.indexOf(plugin.getPlayerManager().getPlayer(player));
            updateTitle();
        }));

        registerClickable(16, ((player1, inventory1, cursor, slot, type) -> {
            plugin.getLocale().getMessage("gui.levels.nametosearch").sendPrefixedMessage(player);
            AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event -> {
                Optional<EPlayer> targetOptional = players.stream()
                        .filter(ePlayer -> ePlayer.getPlayer().getName() != null
                                && ePlayer.getPlayer().getName().toLowerCase().contains(event.getMessage().toLowerCase()))
                        .limit(1).findAny();

                if (!targetOptional.isPresent()) {
                    plugin.getLocale().getMessage("gui.levels.noresults").sendPrefixedMessage(player);
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

        LEVELS,
        MOBKILLS,
        PLAYERKILLS,
        KILLSTREAKS;

        public String getName() {
            return EpicLevels.getInstance().getLocale().getMessage("gui.levels." + name().toLowerCase() + "type").getMessage();
        }
    }

}
