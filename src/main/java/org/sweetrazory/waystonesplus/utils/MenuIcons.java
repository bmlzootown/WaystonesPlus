package org.sweetrazory.waystonesplus.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;

import java.util.List;

/**
 * Shared GUI icons that use the plugin resource pack when the player has it loaded.
 */
public final class MenuIcons {
    private MenuIcons() {
    }

    public static ItemStack returnButton(Player player, String action) {
        return returnButton(player, action, LangManager.returnText);
    }

    public static ItemStack returnButton(Player player, String action, String displayName) {
        boolean usePack = ResourcePackManager.hasPack(player);
        return new ItemBuilder(usePack ? Material.PAPER : Material.BARRIER)
                .displayName(ColoredText.getText(displayName))
                .itemModel(usePack ? model("return") : null)
                .persistentData("action", action)
                .build();
    }

    public static ItemStack settingsButton(Player player, String action) {
        boolean usePack = ResourcePackManager.hasPack(player);
        return new ItemBuilder(usePack ? Material.PAPER : Material.BARRIER)
                .displayName(ColoredText.getText(LangManager.settingsMenuTitle))
                .itemModel(usePack ? model("settings") : null)
                .persistentData("action", action)
                .build();
    }

    public static ItemStack infoButton(Player player, List<String> lore) {
        boolean usePack = ResourcePackManager.hasPack(player);
        return new ItemBuilder(usePack ? Material.PAPER : Material.COPPER_TORCH)
                .displayName(ColoredText.getText(LangManager.teleportInfoTitle))
                .lore(lore)
                .itemModel(usePack ? model("info") : null)
                .build();
    }

    public static ItemStack prevPageButton(Player player) {
        return pageButton(player, "back", LangManager.prevPage, "prevPage", null);
    }

    public static ItemStack prevPageButton(Player player, int page) {
        return pageButton(player, "back", LangManager.prevPage, "prevPage", page);
    }

    public static ItemStack nextPageButton(Player player) {
        return pageButton(player, "next", LangManager.nextPage, "nextPage", null);
    }

    public static ItemStack nextPageButton(Player player, int page) {
        return pageButton(player, "next", LangManager.nextPage, "nextPage", page);
    }

    private static ItemStack pageButton(Player player, String modelId, String displayName, String action, Integer page) {
        boolean usePack = ResourcePackManager.hasPack(player);
        ItemBuilder builder = new ItemBuilder(usePack ? Material.PAPER : Material.ARROW)
                .displayName(ColoredText.getText(displayName))
                .itemModel(usePack ? model(modelId) : null)
                .persistentData("action", action);
        if (page != null) {
            builder.persistentData("page", page);
        }
        return builder.build();
    }

    private static NamespacedKey model(String id) {
        return new NamespacedKey(WaystonesPlus.getInstance(), id);
    }
}
