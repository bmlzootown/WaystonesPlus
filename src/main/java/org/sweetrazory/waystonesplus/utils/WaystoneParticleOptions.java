package org.sweetrazory.waystonesplus.utils;

import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public final class WaystoneParticleOptions {
    public static final int PAGE_SIZE = 28;
    public static final Option[] OPTIONS = buildOptions();

    private WaystoneParticleOptions() {
    }

    private static Option[] buildOptions() {
        return Arrays.stream(Particle.values())
                .filter(Particles::isSupported)
                .sorted(Comparator.comparing(Enum::name))
                .map(p -> new Option(p, iconFor(p), formatName(p)))
                .toArray(Option[]::new);
    }

    private static String formatName(Particle particle) {
        String[] parts = particle.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return name.toString();
    }

    private static Material iconFor(Particle particle) {
        return switch (particle) {
            case ANGRY_VILLAGER -> Material.FIRE_CHARGE;
            case HAPPY_VILLAGER -> Material.EMERALD;
            case HEART -> Material.RED_DYE;
            case NOTE -> Material.NOTE_BLOCK;
            case PORTAL -> Material.OBSIDIAN;
            case ENCHANT -> Material.ENCHANTED_BOOK;
            case FLAME, SOUL_FIRE_FLAME -> Material.BLAZE_POWDER;
            case SOUL -> Material.SOUL_SAND;
            case END_ROD -> Material.END_ROD;
            case DRAGON_BREATH -> Material.DRAGON_BREATH;
            case FIREWORK -> Material.FIREWORK_ROCKET;
            case GLOW -> Material.GLOWSTONE_DUST;
            case GLOW_SQUID_INK -> Material.GLOW_INK_SAC;
            case CHERRY_LEAVES, FALLING_SPORE_BLOSSOM -> Material.CHERRY_LEAVES;
            case SPORE_BLOSSOM_AIR -> Material.SPORE_BLOSSOM;
            case WAX_ON -> Material.HONEYCOMB;
            case WAX_OFF -> Material.HONEYCOMB_BLOCK;
            case SCRAPE -> Material.COPPER_INGOT;
            case ELECTRIC_SPARK -> Material.LIGHTNING_ROD;
            case SONIC_BOOM -> Material.ECHO_SHARD;
            case SCULK_SOUL -> Material.SCULK;
            case SCULK_CHARGE -> Material.SCULK_CATALYST;
            case TRIAL_SPAWNER_DETECTION, TRIAL_SPAWNER_DETECTION_OMINOUS -> Material.TRIAL_SPAWNER;
            case OMINOUS_SPAWNING -> Material.OMINOUS_BOTTLE;
            case RAID_OMEN, TRIAL_OMEN -> Material.OMINOUS_TRIAL_KEY;
            case BUBBLE -> Material.PUFFERFISH;
            case SPLASH -> Material.WATER_BUCKET;
            case FISHING -> Material.FISHING_ROD;
            case DOLPHIN -> Material.HEART_OF_THE_SEA;
            case NAUTILUS -> Material.NAUTILUS_SHELL;
            case CAMPFIRE_COSY_SMOKE, CAMPFIRE_SIGNAL_SMOKE -> Material.CAMPFIRE;
            case WHITE_ASH, ASH -> Material.GRAY_DYE;
            case SNOWFLAKE -> Material.SNOWBALL;
            case DRIPPING_WATER, FALLING_WATER -> Material.WATER_BUCKET;
            case DRIPPING_LAVA, FALLING_LAVA -> Material.LAVA_BUCKET;
            case DRIPPING_HONEY, FALLING_HONEY -> Material.HONEY_BOTTLE;
            case DRIPPING_DRIPSTONE_WATER, DRIPPING_DRIPSTONE_LAVA, FALLING_DRIPSTONE_WATER, FALLING_DRIPSTONE_LAVA -> Material.POINTED_DRIPSTONE;
            case LAVA -> Material.MAGMA_BLOCK;
            case MYCELIUM -> Material.MYCELIUM;
            case CRIT, ENCHANTED_HIT -> Material.IRON_SWORD;
            case WITCH -> Material.POTION;
            case TOTEM_OF_UNDYING -> Material.TOTEM_OF_UNDYING;
            case COMPOSTER -> Material.COMPOSTER;
            case SNEEZE -> Material.BAMBOO;
            case POOF, EXPLOSION, EXPLOSION_EMITTER -> Material.TNT;
            case CLOUD, LARGE_SMOKE, SMOKE -> Material.GUNPOWDER;
            case DUST, DUST_COLOR_TRANSITION -> Material.REDSTONE;
            case BLOCK, BLOCK_MARKER, FALLING_DUST, DUST_PILLAR -> Material.STONE;
            case ITEM -> Material.DIAMOND;
            default -> Material.BLAZE_POWDER;
        };
    }

    public record Option(Particle particle, Material icon, String displayName) {
    }
}
