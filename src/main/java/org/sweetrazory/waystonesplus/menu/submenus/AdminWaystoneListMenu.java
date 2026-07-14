package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.sweetrazory.waystonesplus.enums.Visibility;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.Menu;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.menu.TeleportMenu;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.DB;
import org.sweetrazory.waystonesplus.utils.ItemBuilder;
import org.sweetrazory.waystonesplus.utils.ItemUtils;
import org.sweetrazory.waystonesplus.utils.MenuIcons;
import org.sweetrazory.waystonesplus.waystone.Waystone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminWaystoneListMenu extends Menu {
    private static final int ITEMS_PER_PAGE = 45;

    public AdminWaystoneListMenu(int page) {
        super(54, ColoredText.getText(LangManager.adminListMenuTitle), page);
    }

    private static String getCreatorName(String ownerId) {
        if (ownerId == null || ownerId.isEmpty()) {
            return "Unknown";
        }
        try {
            UUID uuid = UUID.fromString(ownerId);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName();
            return name != null ? name : ownerId;
        } catch (IllegalArgumentException e) {
            return ownerId;
        }
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        List<Waystone> all = DB.getAllWaystones();
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, all.size());

        for (int i = startIndex; i < endIndex; i++) {
            Waystone ws = all.get(i);
            int slot = i - startIndex;
            Material icon = ws.getIcon() != null ? ws.getIcon() : Material.LODESTONE;
            String creatorName = getCreatorName(ws.getOwnerId());
            Visibility vis = ws.getVisibility();
            String visStr = vis != null ? vis.name() : "?";
            List<String> lore = new ArrayList<>();
            lore.add(ColoredText.getText("&7Creator: &f" + creatorName));
            lore.add(ColoredText.getText("&7Visibility: &f" + visStr));
            ItemStack item = new ItemBuilder(icon)
                    .displayName(ColoredText.getText("&6" + ws.getName()))
                    .lore(lore)
                    .persistentData("action", "waystone_info")
                    .persistentData("waystoneId", ws.getId())
                    .build();
            setItem(slot, item);
        }

        if (page > 0) {
            setItem(45, MenuIcons.prevPageButton(player, page - 1));
        }

        if (endIndex < all.size()) {
            setItem(53, MenuIcons.nextPageButton(player, page + 1));
        }

        setItem(49, MenuIcons.returnButton(player, "close"));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event, Waystone waystone) {
        if (event.getInventory() != inventory) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        String action = ItemUtils.getPersistentString(item, "action");
        if ("waystone_info".equals(action)) {
            String waystoneId = ItemUtils.getPersistentString(item, "waystoneId");
            if (waystoneId != null) {
                Waystone target = DB.getWaystone(waystoneId);
                if (target != null) {
                    player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 10, 1);
                    if (event.isLeftClick()) {
                        new TeleportMenu(0).teleportToWaystone(player, target);
                    } else if (event.isRightClick()) {
                        MenuManager.openMenu(player, new SettingsMenu(), target);
                    }
                }
            }
            return;
        }
        super.onInventoryClick(event, waystone);
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action == null) return;
        switch (action) {
            case "close":
                MenuManager.closeMenu(player);
                break;
            case "prevPage": {
                int prevPage = Integer.parseInt(ItemUtils.getPersistentString(item, "page"));
                MenuManager.openMenu(player, new AdminWaystoneListMenu(prevPage), null);
                break;
            }
            case "nextPage": {
                int nextPage = Integer.parseInt(ItemUtils.getPersistentString(item, "page"));
                MenuManager.openMenu(player, new AdminWaystoneListMenu(nextPage), null);
                break;
            }
            default:
                break;
        }
    }
}
