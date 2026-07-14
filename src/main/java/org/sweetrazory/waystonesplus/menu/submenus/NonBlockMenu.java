package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.Menu;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.DB;
import org.sweetrazory.waystonesplus.utils.ItemBuilder;
import org.sweetrazory.waystonesplus.utils.ItemUtils;
import org.sweetrazory.waystonesplus.utils.MenuIcons;
import org.sweetrazory.waystonesplus.waystone.Waystone;

public class NonBlockMenu extends Menu {
    private final Material[] elements = IconMaterials.ITEMS;

    public NonBlockMenu(int page) {
        super(54, ColoredText.getText(LangManager.itemsMenuTitle), page);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName(" ").build();
        ItemStack backButton = MenuIcons.returnButton(player, "menu");
        inventory.setContents(new ItemStack[]{
                filler, filler, filler, filler, filler, filler, filler, filler, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, filler, filler, filler, backButton, filler, filler, filler, filler
        });

        int pageStart = page * IconMaterials.PAGE_SIZE;
        if (pageStart + IconMaterials.PAGE_SIZE < elements.length) {
            setItem(50, MenuIcons.nextPageButton(player));
        }
        if (page > 0) {
            setItem(48, MenuIcons.prevPageButton(player));
        }

        int k = 0;
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int index = pageStart + k;
                if (index >= elements.length) {
                    break;
                }
                Material material = elements[index];
                setItem(i * 9 + j + 1, new ItemBuilder(material)
                        .persistentData("action", "setIcon")
                        .displayName(ColoredText.getText("&6" + material.name()))
                        .build());
                k++;
            }
        }
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action == null) {
            return;
        }

        Menu iconMenu = new IconMenu();
        switch (action) {
            case "menu" -> MenuManager.openMenu(player, iconMenu, waystone);
            case "nextPage" -> {
                page++;
                refresh(player, waystone);
            }
            case "prevPage" -> {
                page--;
                refresh(player, waystone);
            }
            case "setIcon" -> {
                waystone.setIcon(item.getType());
                DB.updateWaystone(waystone);
                MenuManager.openMenu(player, iconMenu, waystone);
            }
        }
    }
}
