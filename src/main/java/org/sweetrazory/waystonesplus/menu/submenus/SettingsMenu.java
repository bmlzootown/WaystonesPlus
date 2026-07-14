package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.Menu;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.menu.TeleportMenu;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.ItemBuilder;
import org.sweetrazory.waystonesplus.utils.ItemUtils;
import org.sweetrazory.waystonesplus.utils.MenuIcons;
import org.sweetrazory.waystonesplus.waystone.Waystone;

import java.util.Arrays;

public class SettingsMenu extends Menu {
    public SettingsMenu() {
        super(27, ColoredText.getText(LangManager.settingsMenuTitle), 0);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        ItemStack renameMenu = new ItemBuilder(Material.ANVIL)
                .persistentData("action", "renameSettings")
                .displayName(ColoredText.getText("&6Rename Waystone"))
                .build();
        ItemStack visibilityMenu = new ItemBuilder(Material.SPYGLASS)
                .persistentData("action", "visibilitySettings")
                .displayName(ColoredText.getText("&6Change Visibility"))
                .build();
        ItemStack particleMenu = new ItemBuilder(Material.MELON_SEEDS)
                .persistentData("action", "particleSettings")
                .displayName(ColoredText.getText("&6Change Particles"))
                .build();
        ItemStack explorerList = new ItemBuilder(Material.PLAYER_HEAD)
                .persistentData("action", "explorerSettings")
                .displayName(ColoredText.getText("&6Explorers list"))
                .build();
        ItemStack typeMenu = new ItemBuilder(Material.YELLOW_GLAZED_TERRACOTTA)
                .displayName(ColoredText.getText("&6Change Waystone Type"))
                .persistentData("action", "typeSettings")
                .build();
        ItemStack iconMenu = new ItemBuilder(Material.GLOW_ITEM_FRAME)
                .displayName(ColoredText.getText("&6Change Waystone Icon"))
                .persistentData("action", "iconSettings")
                .build();
        ItemStack teleportDirectionMenu = new ItemBuilder(Material.COMPASS)
                .displayName(ColoredText.getText("&6Teleport Direction"))
                .persistentData("action", "teleportDirection")
                .build();
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .displayName(" ")
                .build();
        inventory.setContents(Arrays.asList(filler, filler, filler, filler, filler, filler, filler, filler, filler,
                filler, filler, null, null, null, null, null, filler, filler,
                filler, filler, filler, filler, filler, filler, filler, filler, filler).toArray(new ItemStack[0]));

        if (player.isOp() || player.hasPermission("waystonesplus.menu.rename")) {
            setItem(10, renameMenu);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.visibility")) {
            setItem(11, visibilityMenu);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.particle")) {
            setItem(12, particleMenu);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.explorers")) {
            setItem(13, explorerList);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.type")) {
            setItem(14, typeMenu);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.icon")) {
            setItem(15, iconMenu);
        }
        if (player.isOp() || player.hasPermission("waystonesplus.menu.settings")) {
            setItem(16, teleportDirectionMenu);
        }

        setItem(22, MenuIcons.returnButton(player, "teleportMenu"));
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action != null) {
            switch (action) {
                case "teleportMenu":
                    MenuManager.openMenu(player, TeleportMenu.forPlayer(player), waystone);
                    break;
                case "visibilitySettings":
                    Menu visibilitySettingsMenu = new VisibilitySettingsMenu();
                    MenuManager.openMenu(player, visibilitySettingsMenu, waystone);
                    break;
                case "particleSettings":
                    Menu particleMenu = new ParticleMenu(0);
                    MenuManager.openMenu(player, particleMenu, waystone);
                    break;
                case "explorerSettings":
                    Menu exploredMenu = new ExploredMenu(0);
                    MenuManager.openMenu(player, exploredMenu, waystone);
                    break;
                case "typeSettings":
                    Menu typeMenu = new TypeMenu();
                    MenuManager.openMenu(player, typeMenu, waystone);
                    break;
                case "iconSettings":
                    Menu iconMenu = new IconMenu();
                    MenuManager.openMenu(player, iconMenu, waystone);
                    break;
                case "teleportDirection":
                    Menu teleportDirectionMenu = new TeleportDirectionMenu();
                    MenuManager.openMenu(player, teleportDirectionMenu, waystone);
                    break;
                case "renameSettings":
                    Menu renameMenu = new RenameMenu();
                    MenuManager.openMenu(player, renameMenu, waystone);
                    break;
            }
        }
    }
}
