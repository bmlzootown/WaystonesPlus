package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Location;
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

import java.util.Arrays;

public class TeleportDirectionMenu extends Menu {
    public TeleportDirectionMenu() {
        super(27, ColoredText.getText("&6Teleport Direction"), 0);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName(" ").build();
        inventory.setContents(Arrays.asList(filler, filler, filler, filler, filler, filler, filler, filler, filler,
                filler, filler, null, null, null, null, null, filler, filler,
                filler, filler, filler, filler, filler, filler, filler, filler, filler).toArray(new ItemStack[0]));

        // North
        ItemStack north = new ItemBuilder(Material.COMPASS)
                .displayName(ColoredText.getText("&6North"))
                .persistentData("action", "directionNorth")
                .build();
        setItem(11, north);

        // East
        ItemStack east = new ItemBuilder(Material.COMPASS)
                .displayName(ColoredText.getText("&6East"))
                .persistentData("action", "directionEast")
                .build();
        setItem(12, east);

        // South
        ItemStack south = new ItemBuilder(Material.COMPASS)
                .displayName(ColoredText.getText("&6South"))
                .persistentData("action", "directionSouth")
                .build();
        setItem(13, south);

        // West
        ItemStack west = new ItemBuilder(Material.COMPASS)
                .displayName(ColoredText.getText("&6West"))
                .persistentData("action", "directionWest")
                .build();
        setItem(14, west);

        // Use Current Position
        ItemStack currentPos = new ItemBuilder(Material.PLAYER_HEAD)
                .displayName(ColoredText.getText("&6Use Current Position"))
                .persistentData("action", "directionCurrent")
                .build();
        setItem(15, currentPos);

        setItem(22, MenuIcons.returnButton(player, "back"));
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action != null) {
            Waystone waystone = MenuManager.getPlayerWaystone(player);
            if (waystone == null) return;

            Location waystoneLocation = waystone.getLocation();
            String newDirection;
            
            switch (action) {
                case "directionNorth":
                    // User wants to face North, so teleport South of waystone
                    newDirection = "S";
                    break;
                case "directionEast":
                    // User wants to face East, so teleport West of waystone
                    newDirection = "W";
                    break;
                case "directionSouth":
                    // User wants to face South, so teleport North of waystone
                    newDirection = "N";
                    break;
                case "directionWest":
                    // User wants to face West, so teleport East of waystone
                    newDirection = "E";
                    break;
                case "directionCurrent":
                    // Determine closest cardinal direction from player position to waystone
                    // This already gives us the direction FROM waystone TO player, which is correct
                    Location playerLoc = player.getLocation();
                    double waystoneCenterX = waystoneLocation.getX() + 0.5;
                    double waystoneCenterZ = waystoneLocation.getZ() + 0.5;
                    newDirection = DB.positionToCardinalDirection(playerLoc.getX(), playerLoc.getZ(), waystoneCenterX, waystoneCenterZ);
                    break;
                case "back":
                    Menu settingsMenu = new SettingsMenu();
                    MenuManager.openMenu(player, settingsMenu, waystone);
                    return;
                default:
                    return;
            }

            waystone.setTeleportDirection(newDirection);
            DB.updateWaystone(waystone);
            player.sendMessage(ColoredText.getText("&aTeleport location updated!"));

            Menu settingsMenu = new SettingsMenu();
            MenuManager.openMenu(player, settingsMenu, waystone);
        }
    }
}
