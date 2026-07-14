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

import java.util.List;
import java.util.Map;

public class ExploredMenu extends Menu {
    private static final int ITEMS_PER_PAGE = 45;

    public ExploredMenu(int page) {
        super(54, ColoredText.getText(LangManager.explorersMenuTitle), page);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        List<Map<String, String>> explorers = DB.getExplorers(waystone.getId());
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, explorers.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map<String, String> playerData = explorers.get(i);
            ItemStack playerHead = new ItemBuilder(Material.PLAYER_HEAD)
                    .displayName(ColoredText.getText("&6" + playerData.get("playerName")))
                    .build();
            setItem(i - startIndex, playerHead);
        }

        if (page > 0) {
            setItem(45, MenuIcons.prevPageButton(player, page - 1));
        }

        if (endIndex < explorers.size()) {
            setItem(53, MenuIcons.nextPageButton(player, page + 1));
        }

        setItem(49, MenuIcons.returnButton(player, "menu"));
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action != null) {
            if (action.equals("menu")) {
                MenuManager.openMenu(player, new SettingsMenu(), waystone);
            } else if (action.equals("prevPage")) {
                int prevPage = Integer.parseInt(ItemUtils.getPersistentString(item, "page"));
                Menu prevMenu = new ExploredMenu(prevPage);
                MenuManager.openMenu(player, prevMenu, waystone);
            } else if (action.equals("nextPage")) {
                int nextPage = Integer.parseInt(ItemUtils.getPersistentString(item, "page"));
                Menu nextMenu = new ExploredMenu(nextPage);
                MenuManager.openMenu(player, nextMenu, waystone);
            }
        }
    }
}
