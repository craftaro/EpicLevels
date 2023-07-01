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
        super(EpicLevels.getPlugin(EpicLevels.class), "levels");
        setRows(6);

        this.player = player;
        this.plugin = EpicLevels.getPlugin(EpicLevels.class);
        this.plugin.getPlayerManager().getPlayer(player);

        this.playersListLastUpdate = this.plugin.getPlayerManager().getLastUpdate();
        this.players = this.plugin.getPlayerManager().getPlayers();
        this.target = target;

        sort();
        updateTitle();
    }

    private void updateTitle() {
        this.setTitle(this.plugin.getLocale().getMessage("gui.levels.title")
                .processPlaceholder("type", this.sortingBy.getName()).getMessage());
        constructGUI();
    }

    private void sort() {
        this.position = this.target == null ? 0 : this.players.indexOf(this.target);
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
                        this.plugin.getLocale().getMessage("gui.levels.toplevels").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.LEVELS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();
                        this.players = this.plugin.getPlayerManager().getPlayers();
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("mobs", 3,
                GuiUtils.createButtonItem(CompatibleMaterial.SKELETON_SKULL,
                        this.plugin.getLocale().getMessage("gui.levels.topmobs").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.MOBKILLS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(this.plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getMobKills).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("players", 5,
                GuiUtils.createButtonItem(CompatibleMaterial.PLAYER_HEAD,
                        this.plugin.getLocale().getMessage("gui.levels.topplayers").getMessage()),
                (event) -> {
                    Sorting targetSorting = Sorting.PLAYERKILLS;

                    if (shouldUpdatePlayerList(targetSorting)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(this.plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getPlayerKills).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSorting;
                    updateTitle();
                });

        setButton("killstreaks", 6,
                GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD,
                        this.plugin.getLocale().getMessage("gui.levels.topkillstreaks").getMessage()),
                (event) -> {
                    Sorting targetSort = Sorting.KILLSTREAKS;

                    if (shouldUpdatePlayerList(targetSort)) {
                        this.playersListLastUpdate = System.currentTimeMillis();

                        List<EPlayer> tmpPlayers = new ArrayList<>(this.plugin.getPlayerManager().getPlayersUnsorted());
                        tmpPlayers.sort(Comparator.comparingInt(EPlayer::getKillStreak).reversed());
                        this.players = tmpPlayers;
                    }

                    sort();
                    this.sortingBy = targetSort;
                    updateTitle();
                });

        setButton("search", 16, GuiUtils.createButtonItem(CompatibleMaterial.COMPASS,
                        this.plugin.getLocale().getMessage("gui.levels.search").getMessage()),
                (event) ->
                        ChatPrompt.showPrompt(this.plugin, this.player,
                                this.plugin.getLocale().getMessage("gui.levels.nametosearch").getMessage(),
                                response -> {
                                    Optional<EPlayer> targetOptional = Optional.empty();
                                    for (EPlayer ePlayer : this.players) {
                                        if (ePlayer.getPlayer().getName() != null
                                                && ePlayer.getPlayer().getName().toLowerCase().contains(response.getMessage().toLowerCase())) {
                                            targetOptional = Optional.of(ePlayer);
                                            break;
                                        }
                                    }

                                    if (!targetOptional.isPresent()) {
                                        this.plugin.getLocale().getMessage("gui.levels.noresults").sendPrefixedMessage(this.player);
                                        return;
                                    }

                                    this.plugin.getGuiManager().showGUI(this.player, this);
                                    this.target = targetOptional.get();
                                    sort();
                                    updateTitle();
                                }));

        setButton("sorting", 13, GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE,
                        this.plugin.getLocale().getMessage("gui.levels.sortingby").processPlaceholder("type", this.sortingBy.getName()).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.you").getMessage()),
                (event) -> {
                    this.position = this.players.indexOf(this.plugin.getPlayerManager().getPlayer(this.player));
                    updateTitle();
                });

        for (int i = 0; i < 7; ++i) {
            int current = i + (this.position - 3 < 0 ? 0 : (this.position + 3 > (this.players.size() - 1) ? (this.players.size() - 7) : this.position - 3));

            if (current < 0 || current > this.players.size() - 1) {
                continue;
            }
            EPlayer selected = this.players.get(current);
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
            meta.setDisplayName(this.plugin.getLocale().getMessage("gui.levels.name").processPlaceholder("position", current + 1).processPlaceholder("name", targetPlayer.getName()).getMessage());
            meta.setLore(Arrays.asList(this.plugin.getLocale().getMessage("gui.levels.level").processPlaceholder("level", Methods.formatDecimal(selected.getLevel())).getMessage(),
                    this.plugin.getLocale().getMessage("gui.levels.exp").processPlaceholder("exp", Methods.formatDecimal(selected.getExperience())).processPlaceholder("expnext", Methods.formatDecimal(EPlayer.experience(selected.getLevel() + 1))).getMessage(), prog));
            head.setItemMeta(meta);

            int slot = 37 + i;

            if (current == this.position) {
                setItem(slot + 9, GuiUtils.createButtonItem(CompatibleMaterial.ARROW,
                        this.plugin.getLocale().getMessage("gui.levels.selected").getMessage()));
            }

            setButton(slot, head, (event) -> {
                this.position = current;
                updateTitle();
            });

            if (current == this.position) {
                setItem(slot - 9, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD, this.plugin.getLocale().getMessage("gui.levels.stats").getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.totalkills").processPlaceholder("kills", Methods.formatDecimal(selected.getKills())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.playerkills").processPlaceholder("kills", Methods.formatDecimal(selected.getPlayerKills())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.mobkills").processPlaceholder("kills", Methods.formatDecimal(selected.getMobKills())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.deaths").processPlaceholder("deaths", Methods.formatDecimal(selected.getDeaths())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.kdr").processPlaceholder("kdr", Methods.formatDecimal(selected.getDeaths() == 0 ? selected.getPlayerKills() : ((double) selected.getPlayerKills()) / (double) selected.getDeaths())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.killstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getKillStreak())).getMessage(),
                        this.plugin.getLocale().getMessage("gui.levels.bestkillstreak").processPlaceholder("killstreak", Methods.formatDecimal(selected.getBestKillStreak())).getMessage()));
            } else {
                setItem(slot - 9, null);
            }
        }
    }

    private boolean shouldUpdatePlayerList(Sorting sorting) {
        return sorting != this.sortingBy ||
                this.playersListLastUpdate < this.plugin.getPlayerManager().getLastUpdate() ||
                this.playersListLastUpdate < System.currentTimeMillis() - 10_000;
    }

    public enum Sorting {
        LEVELS, MOBKILLS, PLAYERKILLS, KILLSTREAKS;

        public String getName() {
            return EpicLevels.getPlugin(EpicLevels.class)
                    .getLocale()
                    .getMessage("gui.levels." + name().toLowerCase() + "type")
                    .getMessage();
        }
    }
}
