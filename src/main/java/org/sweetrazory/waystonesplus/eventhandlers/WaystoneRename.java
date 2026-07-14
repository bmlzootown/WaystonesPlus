package org.sweetrazory.waystonesplus.eventhandlers;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sweetrazory.waystonesplus.WaystonesPlus;

/**
 * Preview-only handler for waystone rename anvils.
 * Persistence happens when the player confirms the result (see RenameMenu).
 */
public class WaystoneRename {
    public WaystoneRename(PrepareAnvilEvent event) {
        AnvilInventory eventInventory = event.getInventory();
        ItemStack firstSlot = eventInventory.getItem(0);
        ItemStack secondSlot = eventInventory.getItem(1);

        NamespacedKey waystoneIdKey = new NamespacedKey(WaystonesPlus.getInstance(), "waystoneId");
        ItemStack waystoneItem = null;

        if (firstSlot != null && firstSlot.getItemMeta() != null) {
            String id = firstSlot.getItemMeta().getPersistentDataContainer().get(waystoneIdKey, PersistentDataType.STRING);
            if (id != null) {
                waystoneItem = firstSlot;
            }
        }

        if (waystoneItem == null && secondSlot != null && secondSlot.getItemMeta() != null) {
            String id = secondSlot.getItemMeta().getPersistentDataContainer().get(waystoneIdKey, PersistentDataType.STRING);
            if (id != null) {
                waystoneItem = secondSlot;
            }
        }

        if (waystoneItem == null) {
            return;
        }

        Player player = (Player) event.getView().getPlayer();
        boolean playerHasPermission = player.hasPermission("waystonesplus.rename")
                || player.hasPermission("waystonesplus.menu.rename")
                || player.isOp();

        if (!playerHasPermission) {
            event.setResult(null);
            return;
        }

        String anvilText = eventInventory.getRenameText();
        // Do not mutate the input item — that causes rename-text / null flicker.
        ItemStack result = waystoneItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            event.setResult(null);
            return;
        }

        if (anvilText == null || anvilText.isEmpty()) {
            // Keep the current name as the preview until the player types something.
            event.setResult(result);
            eventInventory.setRepairCost(0);
            return;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&6" + anvilText));
        result.setItemMeta(meta);
        event.setResult(result);
        eventInventory.setRepairCost(0);
    }
}
