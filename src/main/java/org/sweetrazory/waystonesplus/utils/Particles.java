package org.sweetrazory.waystonesplus.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Particles {
    private Particles() {
    }

    public static Particle parse(String name) {
        if (name == null || name.equalsIgnoreCase("off")) {
            return null;
        }
        return Particle.valueOf(legacyName(name));
    }

    public static boolean isSupported(Particle particle) {
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) {
            return true;
        }
        if (dataType == Particle.DustOptions.class) {
            return true;
        }
        if (dataType == Particle.DustTransition.class) {
            return true;
        }
        if (BlockData.class.isAssignableFrom(dataType)) {
            return true;
        }
        if (dataType == ItemStack.class) {
            return true;
        }
        return dataType == Float.class || dataType == Integer.class;
    }

    public static void spawn(Player player, Location location, Particle particle) {
        if (particle == null) {
            return;
        }
        Object data = defaultData(particle);
        Class<?> dataType = particle.getDataType();
        if (dataType != Void.class && data == null) {
            return;
        }
        if (data != null) {
            player.spawnParticle(particle, location, 1, 0.5, 0.5, 0.5, data);
        } else {
            player.spawnParticle(particle, location, 1, 0.5, 0.5, 0.5);
        }
    }

    private static Object defaultData(Particle particle) {
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) {
            return null;
        }
        if (dataType == Particle.DustOptions.class) {
            return new Particle.DustOptions(Color.fromRGB(140, 90, 255), 1.2F);
        }
        if (dataType == Particle.DustTransition.class) {
            return new Particle.DustTransition(Color.fromRGB(255, 80, 80), Color.fromRGB(80, 120, 255), 1.2F);
        }
        if (BlockData.class.isAssignableFrom(dataType)) {
            return Material.STONE.createBlockData();
        }
        if (dataType == ItemStack.class) {
            return new ItemStack(Material.DIAMOND);
        }
        if (dataType == Float.class) {
            return 1.0F;
        }
        if (dataType == Integer.class) {
            return 1;
        }
        return null;
    }

    private static String legacyName(String name) {
        return switch (name.toUpperCase()) {
            case "ENCHANTMENT_TABLE" -> "ENCHANT";
            case "VILLAGER_ANGRY" -> "ANGRY_VILLAGER";
            case "VILLAGER_HAPPY" -> "HAPPY_VILLAGER";
            default -> name.toUpperCase();
        };
    }
}
