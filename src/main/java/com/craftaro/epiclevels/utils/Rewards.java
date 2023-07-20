package com.craftaro.epiclevels.utils;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.math.Eval;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epiclevels.EpicLevels;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Rewards {
    public static void run(List<String> rewards, Player player, int level, boolean last) {
        if (level == -1 && EpicLevels.getPlugin(EpicLevels.class).getLevelManager().getLevel(level).getRewards().stream()
                .anyMatch(line -> line.contains("OVERRIDE"))) {
            return;
        }
        rewards.forEach(s -> {
            try {
                String line = replace(player, level, s.trim());
                switch (s.split(" ")[0]) {
                    case "MSG":
                        if (last) {
                            EpicLevels.getPlugin(EpicLevels.class).getLocale().newMessage(line.trim()).sendPrefixedMessage(player);
                        }
                        break;
                    case "BROADCAST":
                        Bukkit.broadcastMessage(EpicLevels.getPlugin(EpicLevels.class).getLocale().newMessage(line.trim()).getMessage());
                        break;
                    case "CMD":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("/", "").trim());
                        break;
                    case "ITEM":
                        String[] args = line.split(" ");
                        int amount = (int) new Eval(line.replace(args[0], "").trim(), "").parse();
                        ItemStack item = new ItemStack(Material.valueOf(args[0].toUpperCase()), amount);
                        Collection<ItemStack> items = player.getInventory().addItem(item).values();
                        items.forEach(itemStack ->
                                player.getWorld().dropItemNaturally(player.getLocation(), itemStack));
                        break;
                    case "ECONOMY":
                        EconomyManager.deposit(player, Double.parseDouble(line.replace("$", "")));
                        break;
                    case "HEAL":
                        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        } else {
                            player.setHealth(20);
                        }
                        break;
                    case "FIREWORK":
                        if (last) {
                            launchRandomFirework(player.getLocation().clone().add(0, 2, 0));
                        }
                        break;
                    case "SOUND":
                        if (last) {
                            XSound.matchXSound(line.trim()).ifPresent(sound -> sound.play(player, 1, 1));
                        }
                        break;
                    case "TITLE":
                        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                            player.sendTitle(TextUtils.formatText(line.trim()), null, 20, 100, 20);
                        } else {
                            player.sendTitle(TextUtils.formatText(line.trim()), null);
                        }
                    case "SUBTITLE":
                        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                            player.sendTitle(null, TextUtils.formatText(line.trim()), 20, 100, 20);
                        } else {
                            player.sendTitle(null, TextUtils.formatText(line.trim()));
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String replace(Player player, int value, String line) {
        return line.replace("MSG", "")
                .replace("BROADCAST", "")
                .replace("SUBTITLE", "")
                .replace("TITLE", "")
                .replace("CMD", "")
                .replace("ITEM", "")
                .replace("ECONOMY", "")
                .replace("SOUND", "")
                .replace("%streak%", String.valueOf(value))
                .replace("%level%", String.valueOf(value))
                .replace("%player%", player.getName())
                .trim();
    }

    private static void launchRandomFirework(Location loc) {
        Random random = new Random();

        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                .withColor(Color.fromRGB(random.nextInt(0xFFFFFF)))
                .withFade(Color.fromRGB(random.nextInt(0xFFFFFF)))
                .build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }
}
