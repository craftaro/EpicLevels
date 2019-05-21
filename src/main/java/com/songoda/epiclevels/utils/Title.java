package com.songoda.epiclevels.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class Title {

    private static Class<?> chatComponentDeclared = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0];
    private static Class<?> packetPlayOutTitle = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0];
    private static Class<?> chatComponent = getNMSClass("IChatBaseComponent");

    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Object chatTitle, chatSubtitle, titlePacket, subtitlePacket;
            Constructor<?> subtitleConstructor;
            Method method = chatComponentDeclared.getMethod("a", new Class[]{String.class});
            if (title != null) {
                // Times packet
                Object times = packetPlayOutTitle.getField("TIMES").get(null);
                chatTitle = method.invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{packetPlayOutTitle, chatComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                titlePacket = subtitleConstructor.newInstance(new Object[]{times, chatTitle, fadeIn, stay, fadeOut});
                sendPacket(player, titlePacket);
                // Title packet
                times = packetPlayOutTitle.getField("TITLE").get(null);
                chatTitle = method.invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{packetPlayOutTitle, chatComponent});
                titlePacket = subtitleConstructor.newInstance(new Object[]{times, chatTitle});
                sendPacket(player, titlePacket);
            }
            if (subtitle != null) {
                // Times packet
                Object times = packetPlayOutTitle.getField("TIMES").get(null);
                chatSubtitle = method.invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{packetPlayOutTitle, chatComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{times, chatSubtitle, fadeIn, stay, fadeOut});
                sendPacket(player, subtitlePacket);
                // Subtitle packet
                times = packetPlayOutTitle.getField("SUBTITLE").get(null);
                chatSubtitle = method.invoke((Object) null, new Object[]{"{\"text\":\"" + subtitle + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{packetPlayOutTitle, chatComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{times, chatSubtitle, fadeIn, stay, fadeOut});
                sendPacket(player, subtitlePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearTitle(Player player) {
        sendTitle(player, "", "", 0, 0, 0);
    }

    public static void sendTabTitle(Player player, String header, String footer) {
        if (header == null)
            header = "";
        if (footer == null)
            footer = "";
        try {
            Method method = chatComponentDeclared.getMethod("a", String.class);
            Object tabHeader = method.invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = method.invoke(null, "{\"text\":\"" + footer + "\"}");
            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
            Object packet = titleConstructor.newInstance();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, tabHeader);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, tabFooter);
            } catch (Exception e) {
                Field a = packet.getClass().getDeclaredField("header");
                a.setAccessible(true);
                a.set(packet, tabHeader);
                Field b = packet.getClass().getDeclaredField("footer");
                b.setAccessible(true);
                b.set(packet, tabFooter);
            }
            sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Builder {

        /**
         * The default number of ticks for the title to fade out.
         */
        public static final int DEFAULT_FADE_OUT = 20;
        /**
         * The default number of ticks for the title to fade in.
         */
        public static final int DEFAULT_FADE_IN = 20;
        /**
         * The default number of ticks for the title to stay.
         */
        public static final int DEFAULT_STAY = 200;
        private int fadeIn, stay, fadeOut;
        private String title, subtitle;

        public Builder() {
            this.fadeOut = DEFAULT_FADE_OUT;
            this.fadeIn = DEFAULT_FADE_IN;
            this.stay = DEFAULT_STAY;
            this.subtitle = "";
            this.title = "";
        }

        public Builder(String title, String subtitle) {
            this.fadeOut = DEFAULT_FADE_OUT;
            this.fadeIn = DEFAULT_FADE_IN;
            this.stay = DEFAULT_STAY;
            this.subtitle = subtitle;
            this.title = title;
        }

        /**
         * Sets the title to the given text.
         *
         * @param title the title text
         * @return this builder instance
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the subtitle to the given text.
         *
         * @param subtitle the title text
         * @return this builder instance
         */
        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * Sets the number of ticks for the title to fade out.
         *
         * @param fadeOut the number of ticks to fade out
         * @return this builder instance
         */
        public Builder fadeOut(int fadeOut) {
            if (fadeOut >= 0)
                fadeOut = DEFAULT_FADE_OUT;
            this.fadeOut = fadeOut;
            return this;
        }

        /**
         * Sets the number of ticks for the title to fade in
         *
         * @param fadeIn the number of ticks to fade in
         * @return this builder instance
         */
        public Builder fadeIn(int fadeIn) {
            if (fadeIn >= 0)
                fadeIn = DEFAULT_FADE_IN;
            this.fadeIn = fadeIn;
            return this;
        }

        /**
         * Sets the number of ticks for the title to stay.
         *
         * @param stay the number of ticks to stay
         * @return this builder instance
         */
        public Builder stay(int stay) {
            if (stay >= 0)
                stay = DEFAULT_STAY;
            this.stay = stay;
            return this;
        }

        /**
         * Send a title based on the values in the builder to multiple players.
         *
         * @param players the players to send the title to
         */
        public void send(Player... players) {
            for (Player player : players)
                send(player);
        }

        /**
         * Send a title based on the values in the builder.
         *
         * @param player the player to send the title to
         */
        public void send(Player player) {
            if (!title.equals("") && !subtitle.equals(""))
                return;
            if (player == null)
                return;
            sendTitle(player, Methods.formatText(title), Methods.formatTitle(subtitle), fadeIn, stay, fadeOut);
        }
    }

}
