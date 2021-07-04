package com.songoda.epiclevels.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.settings.Settings;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GUILevels extends CustomizableGui {

    private final EpicLevels plugin;
    private int position;

    private final Player player;

    private EPlayer target;

    private List<EPlayer> players;
    private long playersListLastUpdate;

    private Sorting sortingBy = Sorting.LEVELS;

    public GUILevels(Player player, EPlayer target) {
        super(EpicLevels.getInstance(), "levels");
        setRows(6);

        this.player = player;
        plugin = EpicLevels.getInstance();
        plugin.getPlayerManager().getPlayer(player);

        this.playersListLastUpdate = plugin.getPlayerManager().getLastUpdate();
        this.players = plugin.getPlayerManager().getPlayers();
        this.target = target;

        sort();
        updateTitle();
    }

    private void updateTitle() {
        this.setTitle(plugin.getLocale().getMessage("gui.levels.title")
                .processPlaceholder("type", sortingBy.getName()).getMessage());
        constructGUI();
    }

    private void sort() {
        this.position = target == null ? 0 : players.indexOf(target);
    }

    private void constructGUI() {
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        setDefaultItem(null);
        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);
        mirrorFill("mirrorfill_4", 2, 0, true, true, glass3);
        mirrorFill("mirrorfill_5", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_6", 0, 3, true, true, glass3);
        mirrorFill("mirrorfill_7", 0, 4, true, true, glass3);

        setButton("levels", 2,
                GuiUtils.createButtonItem(CompatibleMaterial.EXPERIENCE_BOTTLE,
                        plugin.getLocale().getMessage("gui.levels.toplevels").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.LEVELS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();
                        this.players = plugin.getPlayerManager().getPlayers();
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("mobs", 3,
                GuiUtils.createButtonItem(CompatibleMaterial.SKELETON_SKULL,
                        plugin.getLocale().getMessage("gui.levels.topmobs").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.MOBKILLS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getMobKills).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("players", 5,
                GuiUtils.createButtonItem(CompatibleMaterial.PLAYER_HEAD,
                        plugin.getLocale().getMessage("gui.levels.topplayers").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.PLAYERKILLS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getPlayerKills).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("killstreaks", 6,
                GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD,
                        plugin.getLocale().getMessage("gui.levels.topkillstreaks").getMessage()),
                (event) -> {
                    Sorting targetSort = Sorting.KILLSTREAKS;

                    if (shouldUpdatePlayerList(targetSort)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getKillstreak).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSort;
                    updateTitle();
                });

        setButton("search", 16, GuiUtils.createButtonItem(CompatibleMaterial.COMPASS,
                        plugin.getLocale().getMessage("gui.levels.search").getMessage()),
                (event) ->
                        ChatPrompt.showPrompt(plugin, player,
                                plugin.getLocale().getMessage("gui.levels.nametosearch").getMessage(),
                                response -> {
                                    Optional<EPlayer> targetOptional = Optional.empty();
                                    for (EPlayer ePlayer : players) {
                                        if (ePlayer.getPlayer().getName() != null
                                                && ePlayer.getPlayer().getName().toLowerCase().contains(response.getMessage().toLowerCase())) {
                                            targetOptional = Optional.of(ePlayer);
                                            break;
                                        }
                                    }

                                    if (!targetOptional.isPresent()) {
                                        plugin.getLocale().getMessage("gui.levels.noresults").sendPrefixedMessage(player);
                                        return;
                                    }

                                    plugin.getGuiManager().showGUI(player, this);
                                    target = targetOptional.get();
                                    sort();
                                    updateTitle();
                                }));

        setButton("sorting", 13, GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE,
                        plugin.getLocale().getMessage("gui.levels.sortingby").processPlaceholder("type", sortingBy.getName()).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.you").getMessage()),
                (event) -> {
                    this.position = players.indexOf(plugin.getPlayerManager().getPlayer(player));
                    updateTitle();
                });

        for (int i = 0; i < 7; ++i) {
            int current = i + (position - 3 < 0 ? 0 : (position + 3 > (players.size() - 1) ? (players.size() - 7) : position - 3));

            if (current < 0 || current > players.size() - 1) {
                continue;
            }
            EPlayer selected = players.get(current);
            if (selected.getPlayer() == null || selected.getPlayer().getName() == null) {
                continue;
            }

            OfflinePlayer targetPlayer = selected.getPlayer();

            double exp = selected.getExperience() - EPlayer.experience(selected.getLevel());

            double nextLevel = EPlayer.experience(selected.getLevel() + 1) - EPlayer.experience(selected.getLevel());

            String prog = TextUtils.formatText(Methods.generateProgressBar(exp, nextLevel, false));

            ItemStack head = CompatibleMaterial.PLAYER_HEAD.getItem();
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            assert meta != null;
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                meta.setOwningPlayer(targetPlayer);
            } else {
                //noinspection deprecation
                meta.setOwner(targetPlayer.getName());
            }
            meta.setDisplayName(plugin.getLocale().getMessage("gui.levels.name").processPlaceholder("position", current + 1).processPlaceholder("name", targetPlayer.getName()).getMessage());
            meta.setLore(Arrays.asList(plugin.getLocale().getMessage("gui.levels.level").processPlaceholder("level", Methods.formatDecimal(selected.getLevel())).getMessage(),
                    plugin.getLocale().getMessage("gui.levels.exp").processPlaceholder("exp", Methods.formatDecimal(selected.getExperience())).processPlaceholder("expnext", Methods.formatDecimal(EPlayer.experience(selected.getLevel() + 1))).getMessage(), prog));
            head.setItemMeta(meta);

            int slot = 37 + i;

            if (current == position) {
                setItem(slot + 9, GuiUtils.createButtonItem(CompatibleMaterial.ARROW,
                        plugin.getLocale().getMessage("gui.levels.selected").getMessage()));
            }

            setButton(slot, head, (event) -> {
                position = current;
                updateTitle();
            });

            if (current == position) {
                setItem(slot - 9, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD, plugin.getLocale().getMessage("gui.levels.stats").getMessage(),
                        plugin.getLocale().getMessage("gui.levels.totalkills").processPlaceholder("kills", Methods.formatDecimal(selected.getKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.playerkills").processPlaceholder("kills", Methods.formatDecimal(selected.getPlayerKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.mobkills").processPlaceholder("kills", Methods.formatDecimal(selected.getMobKills())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.deaths").processPlaceholder("deaths", Methods.formatDecimal(selected.getDeaths())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.kdr").processPlaceholder("kdr", Methods.formatDecimal(selected.getDeaths() == 0 ? selected.getPlayerKills() : ((double) selected.getPlayerKills()) / (double) selected.getDeaths())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.killstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getKillstreak())).getMessage(),
                        plugin.getLocale().getMessage("gui.levels.bestkillstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getBestKillstreak())).getMessage()));
            } else {
                setItem(slot - 9, null);
            }
        }
    }

    private boolean shouldUpdatePlayerList(Sorting sorting) {
        boolean result = sorting != this.sortingBy ||
                this.playersListLastUpdate < this.plugin.getPlayerManager().getLastUpdate() ||
                this.playersListLastUpdate < System.currentTimeMillis() - 10_000 /* Wait at least 10 seconds otherwise */;

        return result;
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
