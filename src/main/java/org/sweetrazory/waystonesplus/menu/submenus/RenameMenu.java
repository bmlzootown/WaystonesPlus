package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.items.WaystoneSummonItem;
import org.sweetrazory.waystonesplus.menu.Menu;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.DB;
import org.sweetrazory.waystonesplus.waystone.Waystone;

public class RenameMenu extends Menu {
    private AnvilInventory anvilInventory;

    public RenameMenu() {
        super(27, ColoredText.getText("&6Rename Waystone"), 0);
    }

    @Override
    public void open(Player player, Waystone waystone) {
        // Don't call super.open(): this menu uses a real anvil view instead of
        // the chest inventory created by the base class.
        this.waystone = waystone;
        initializeItems(player, waystone);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        ItemStack waystoneItem = WaystoneSummonItem.getLodestoneHead(
                waystone.getName(),
                waystone.getType(),
                null,
                null,
                waystone.getVisibility()
        );

        // Tag the placeholder so PrepareAnvil can identify it for preview only.
        ItemMeta meta = waystoneItem.getItemMeta();
        if (meta != null) {
            NamespacedKey waystoneIdKey = new NamespacedKey(WaystonesPlus.getInstance(), "waystoneId");
            meta.getPersistentDataContainer().set(waystoneIdKey, PersistentDataType.STRING, waystone.getId());
            waystoneItem.setItemMeta(meta);
        }

        InventoryView view = player.openAnvil(null, true);
        if (view == null) {
            return;
        }

        anvilInventory = (AnvilInventory) view.getTopInventory();
        anvilInventory.setItem(0, waystoneItem);

        MenuManager.setPlayerWaystone(player, waystone);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event, Waystone waystone) {
        if (anvilInventory == null || event.getInventory() != anvilInventory) {
            return;
        }

        // The anvil item is a placeholder; never let the player take it.
        if (event.getClickedInventory() == anvilInventory || event.isShiftClick()) {
            event.setCancelled(true);
        }

        if (event.getRawSlot() != 2) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String renameText = anvilInventory.getRenameText();
        if (renameText == null || renameText.isBlank()) {
            player.sendMessage(ColoredText.getText("&cEnter a new name first."));
            return;
        }

        String plainName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', renameText)).trim();
        if (plainName.isEmpty() || plainName.equalsIgnoreCase("null")) {
            player.sendMessage(ColoredText.getText("&cInvalid waystone name."));
            return;
        }

        waystone.setName(plainName);
        DB.updateWaystone(waystone);

        anvilInventory.clear();
        player.closeInventory();
        player.sendMessage(ColoredText.getText("&aWaystone renamed!"));

        // Defer reopen so the anvil close finishes cleanly on Paper.
        org.bukkit.Bukkit.getScheduler().runTask(WaystonesPlus.getInstance(), () ->
                MenuManager.openMenu(player, new SettingsMenu(), waystone));
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Prevent the placeholder waystone item from being returned to the
        // player when the anvil closes.
        if (anvilInventory != null && event.getInventory() == anvilInventory) {
            anvilInventory.clear();
        }
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        // Unused; clicks are handled in onInventoryClick above.
    }
}
