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

    private DecimalFormat decimalFormat = new DecimalFormat("###,###.###");

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
        init(plugin.getLocale().getMessage("gui.levels.title", sortingBy.getName()), 54);
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


        createButton(2, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.EXPERIENCE_BOTTLE : Material.valueOf("EXP_BOTTLE"), plugin.getLocale().getMessage("gui.levels.toplevels"));
        createButton(3, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SKELETON_SKULL : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 0), plugin.getLocale().getMessage("gui.levels.topmobs"));
        createButton(5, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3), plugin.getLocale().getMessage("gui.levels.topplayers"));
        createButton(6, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.levels.topkillstreaks"));
        createButton(16, Material.COMPASS, plugin.getLocale().getMessage("gui.levels.search"));

        createButton(13, Material.REDSTONE, plugin.getLocale().getMessage("gui.levels.sortingby", sortingBy.getName()),
                plugin.getLocale().getMessage("gui.levels.you"));

        for (int i = 0; i < 7; i++) {
            int current = i + (position - 3 < 0 ? 0 : (position + 3 > (players.size() - 1) ? (players.size() - 7) : position - 3));
            if (current > (players.size() - 1)) break;
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
                createButton(slot + 9, Material.ARROW, plugin.getLocale().getMessage("gui.levels.selected"));

            createButton(slot, head, plugin.getLocale().getMessage("gui.levels.name", current + 1, targetPlayer.getName()),
                    plugin.getLocale().getMessage("gui.levels.level", decimalFormat.format(selected.getLevel())),
                    plugin.getLocale().getMessage("gui.levels.exp", decimalFormat.format(selected.getExperience()), decimalFormat.format(EPlayer.experience(selected.getLevel() + 1))),
                    prog);

            if (current == position)
                createButton(slot - 9, Material.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.levels.stats"),
                        plugin.getLocale().getMessage("gui.levels.totalkills", decimalFormat.format(selected.getKills())),
                        plugin.getLocale().getMessage("gui.levels.playerkills", decimalFormat.format(selected.getPlayerKills())),
                        plugin.getLocale().getMessage("gui.levels.mobkills", decimalFormat.format(selected.getMobKills())),
                        plugin.getLocale().getMessage("gui.levels.deaths", decimalFormat.format(selected.getDeaths())),
                        plugin.getLocale().getMessage("gui.levels.kdr", decimalFormat.format(selected.getDeaths() == 0 ? selected.getPlayerKills() : ((double) selected.getPlayerKills()) / selected.getDeaths())),
                        plugin.getLocale().getMessage("gui.levels.killstreak", decimalFormat.format(selected.getKillstreak())),
                        plugin.getLocale().getMessage("gui.levels.bestkillstreak", decimalFormat.format(selected.getBestKillstreak())));

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
            player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("gui.levels.nametosearch"));
            AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event -> {
                Optional<EPlayer> targetOptional = players.stream()
                        .filter(ePlayer -> ePlayer.getPlayer().getName() != null
                                && ePlayer.getPlayer().getName().toLowerCase().contains(event.getMessage().toLowerCase()))
                        .limit(1).findAny();

                if (!targetOptional.isPresent()) {
                    player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("gui.levels.noresults"));
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
            return EpicLevels.getInstance().getLocale().getMessage("gui.levels." + name().toLowerCase() + "type");
        }
    }

}
