package com.songoda.epiclevels.levels;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.utils.Methods;
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

public class Level {

    private final int level;
    private List<String> rewards;

    private static ScriptEngine engine = null;

    public Level(int level, List<String> rewards) {
        this.level = level;
        this.rewards = rewards;

        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
        }
    }

    public void run(Player player, int level, boolean last) {
        if (level == -1 && EpicLevels.getInstance().getLevelManager().getLevel(level).rewards.stream()
                .anyMatch(line -> line.contains("OVERRIDE")))
            return;
        rewards.forEach(s -> {
            try {
                String line = replace(player, level, s.trim());
                switch (s.split(" ")[0]) {
                    case "MSG":
                        if (last)
                            player.sendMessage(EpicLevels.getInstance().getReferences().getPrefix() +
                                    Methods.formatText(line.trim()));
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
                        EpicLevels.getInstance().getEconomy().AddToBalance(player, Double.parseDouble(line.replace("$", "")));
                        break;
                    case "HEAL":
                        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        break;
                    case "FIREWORK":
                        if (last)
                            launchRandomFirework(player.getLocation().clone().add(0, 2, 0));
                        break;
                    case "SOUND":
                        if (last)
                            player.playSound(player.getLocation(), Sound.valueOf(line), 1L, 1L);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public int getLevel() {
        return level;
    }

    private String replace(Player player, int level, String line) {
        line = line.replace("MSG", "")
                .replace("CMD", "")
                .replace("ITEM", "")
                .replace("ECONOMY", "")
                .replace("SOUND", "")
                .replace("%level%", String.valueOf(level))
                .replace("%player%", player.getName())
                .trim();
        return line;
    }


    public static void launchRandomFirework(Location loc){
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
