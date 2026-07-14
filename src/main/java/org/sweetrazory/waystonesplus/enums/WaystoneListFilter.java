package org.sweetrazory.waystonesplus.enums;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.sweetrazory.waystonesplus.WaystonesPlus;

public enum WaystoneListFilter {
    ALL(Material.COMPASS, "tab_all"),
    GLOBAL(Material.YELLOW_CONCRETE, "tab_global"),
    OWNED(Material.CHEST, "tab_owned"),
    EXPLORED(Material.MAP, "tab_explored"),
    FAVORITES(Material.GOLDEN_APPLE, "tab_favorites");

    private final Material material;
    private final String modelId;

    WaystoneListFilter(Material material, String modelId) {
        this.material = material;
        this.modelId = modelId;
    }

    public Material getMaterial() {
        return material;
    }

    public NamespacedKey getItemModel(boolean selected) {
        String id = selected ? modelId + "_selected" : modelId;
        return new NamespacedKey(WaystonesPlus.getInstance(), id);
    }

    public static WaystoneListFilter fromString(@NotNull String value) {
        try {
            return WaystoneListFilter.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
