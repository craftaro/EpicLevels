package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Rewards {

    private static ScriptEngine engine = null;

    public static void run(List<String> rewards, Player player, int level, boolean last) {
        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
        }

        if (level == -1 && EpicLevels.getInstance().getLevelManager().getLevel(level).getRewards().stream()
                .anyMatch(line -> line.contains("OVERRIDE")))
            return;
        rewards.forEach(s -> {
            try {
                String line = replace(player, level, s.trim());
                switch (s.split(" ")[0]) {
                    case "MSG":
                        if (last)
                            EpicLevels.getInstance().getLocale().newMessage(line.trim()).sendPrefixedMessage(player);
                        break;
                    case "BROADCAST":
                        Bukkit.broadcastMessage(EpicLevels.getInstance().getLocale().newMessage(line.trim()).getMessage());
                        break;
                    case "CMD":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("/", "").trim());
                        break;
                    case "ITEM":
                        String[] args = line.split(" ");
                        int amount = Integer.parseInt(engine.eval(line.replace(args[0], "").trim()).toString());
                        ItemStack item = new ItemStack(Material.valueOf(args[0].toUpperCase()), amount);
                        Collection<ItemStack> items = player.getInventory().addItem(item).values();
                        items.forEach(itemStack ->
                                player.getWorld().dropItemNaturally(player.getLocation(), itemStack));
                        break;
                    case "ECONOMY":
                        EpicLevels.getInstance().getEconomy().deposit(player, Double.parseDouble(line.replace("$", "")));
                        break;
                    case "HEAL":
                        if (EpicLevels.getInstance().isServerVersionAtLeast(ServerVersion.V1_9))
                            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        else
                            player.setHealth(20);
                        break;
                    case "FIREWORK":
                        if (last)
                            launchRandomFirework(player.getLocation().clone().add(0, 2, 0));
                        break;
                    case "SOUND":
                        if (last)
                            player.playSound(player.getLocation(), Sound.valueOf(line.trim()), 1L, 1L);
                        break;
                    case "TITLE":
                        Title.sendTitle(player, Methods.formatText(line.trim()), null, 20, 100, 20);
                        break;
                    case "SUBTITLE":
                        Title.sendTitle(player, null, Methods.formatText(line.trim()), 20, 100, 20);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String replace(Player player, int value, String line) {
        line = line.replace("MSG", "")
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
        return line;
    }


    private static void launchRandomFirework(Location loc){
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
