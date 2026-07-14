package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Comparator;

public final class IconMaterials {
    public static final int PAGE_SIZE = 28;
    public static final Material[] BLOCKS = buildBlockIcons();
    public static final Material[] ITEMS = buildItemIcons();

    private IconMaterials() {
    }

    private static Material[] buildBlockIcons() {
        return Arrays.stream(Material.values())
                .filter(m -> m.isBlock() && isUsableIcon(m))
                .sorted(Comparator.comparing(Enum::name))
                .toArray(Material[]::new);
    }

    private static Material[] buildItemIcons() {
        return Arrays.stream(Material.values())
                .filter(m -> m.isItem() && !m.isBlock() && isUsableIcon(m))
                .sorted(Comparator.comparing(Enum::name))
                .toArray(Material[]::new);
    }

    private static boolean isUsableIcon(Material material) {
        if (material.isLegacy() || material.isAir()) {
            return false;
        }

        String name = material.name();
        if (name.startsWith("POTTED_")) {
            return false;
        }
        if (name.endsWith("_WALL_HEAD") || name.endsWith("_WALL_SIGN") || name.endsWith("_WALL_BANNER")) {
            return false;
        }

        return switch (material) {
            case WATER, LAVA, FIRE, SOUL_FIRE, BUBBLE_COLUMN, MOVING_PISTON, PISTON_HEAD, END_PORTAL,
                 NETHER_PORTAL, END_GATEWAY, STRUCTURE_VOID, LIGHT, CAVE_VINES, CAVE_VINES_PLANT,
                 WEEPING_VINES, WEEPING_VINES_PLANT, TWISTING_VINES, TWISTING_VINES_PLANT, KELP,
                 KELP_PLANT, SEAGRASS, TALL_SEAGRASS, BAMBOO_SAPLING, SUGAR_CANE, ATTACHED_MELON_STEM,
                 ATTACHED_PUMPKIN_STEM, MELON_STEM, PUMPKIN_STEM, BEETROOTS, CARROTS, POTATOES, WHEAT,
                 NETHER_WART, COCOA, SWEET_BERRY_BUSH, TORCHFLOWER, PITCHER_CROP, BIG_DRIPLEAF_STEM,
                 PINK_PETALS, SUNFLOWER, LILAC, ROSE_BUSH, PEONY, TALL_GRASS, LARGE_FERN -> false;
            default -> material.isItem();
        };
    }
}
