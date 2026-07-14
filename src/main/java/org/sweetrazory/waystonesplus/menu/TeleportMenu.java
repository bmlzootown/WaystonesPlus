package org.sweetrazory.waystonesplus.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.enums.Visibility;
import org.sweetrazory.waystonesplus.enums.WaystoneListFilter;
import org.sweetrazory.waystonesplus.memoryhandlers.ConfigManager;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.submenus.SettingsMenu;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.DB;
import org.sweetrazory.waystonesplus.utils.ItemBuilder;
import org.sweetrazory.waystonesplus.utils.ItemUtils;
import org.sweetrazory.waystonesplus.utils.MenuIcons;
import org.sweetrazory.waystonesplus.utils.ResourcePackManager;
import org.sweetrazory.waystonesplus.waystone.Waystone;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TeleportMenu extends Menu {
    private static final int[] TAB_SLOTS = {2, 3, 4, 5, 6};
    private static final WaystoneListFilter[] TAB_FILTERS = {
            WaystoneListFilter.ALL,
            WaystoneListFilter.GLOBAL,
            WaystoneListFilter.OWNED,
            WaystoneListFilter.EXPLORED,
            WaystoneListFilter.FAVORITES
    };

    private WaystoneListFilter filter;

    public TeleportMenu(int page) {
        this(page, WaystoneListFilter.ALL);
    }

    public TeleportMenu(int page, WaystoneListFilter filter) {
        super(45, ColoredText.getText(LangManager.teleportMenuTitle), page);
        this.filter = filter != null ? filter : WaystoneListFilter.ALL;
    }

    public static TeleportMenu forPlayer(Player player) {
        return new TeleportMenu(0, DB.getDefaultTeleportFilter(player.getUniqueId().toString()));
    }

    /** Rebuild contents without closing the inventory (keeps the cursor in place). */
    public void refreshPublic(Player player) {
        refresh(player);
    }

    private void refresh(Player player) {
        initializeItems(player, this.waystone);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        String playerId = player.getUniqueId().toString();
        WaystoneListFilter defaultFilter = DB.getDefaultTeleportFilter(playerId);

        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName(" ").build();
        inventory.setContents(Arrays.asList(
                filler, filler, filler, filler, filler, filler, filler, filler, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, filler, filler, filler, filler, filler, filler, filler, filler
        ).toArray(new ItemStack[0]));

        for (int i = 0; i < TAB_FILTERS.length; i++) {
            setItem(TAB_SLOTS[i], buildTabItem(player, TAB_FILTERS[i], defaultFilter));
        }

        List<Waystone> waystones = DB.getWaystones(playerId, page, 21, waystone != null ? waystone.getId() : null, filter);
        int listSize;
        try {
            listSize = DB.getWaystonesSize(playerId, waystone != null ? waystone.getId() : null, filter);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int k = 0;
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 8; j++) {
                if (waystones.size() > k) {
                    Waystone currWaystone = waystones.get(k);
                    boolean favorite = DB.isFavorite(playerId, currWaystone.getId());
                    List<String> lore = new ArrayList<>();
                    lore.add(ColoredText.getText(favorite ? LangManager.teleportFavorite : LangManager.teleportNotFavorite));
                    lore.add(ColoredText.getText(LangManager.teleportFavoriteHint));
                    lore.add(" ");
                    lore.addAll(waystoneDetailLore(currWaystone));

                    String displayName = currWaystone.getName();
                    if (favorite) {
                        displayName = "&6★ " + displayName;
                    }

                    ItemStack waystoneItem = new ItemBuilder(currWaystone.getIcon())
                            .displayName(ColoredText.getText(displayName))
                            .lore(lore)
                            .persistentData("waystoneId", currWaystone.getId())
                            .persistentData("action", "teleport")
                            .build();
                    setItem(i * 9 + j, waystoneItem);
                    k++;
                }
            }
        }

        if (listSize > 21 * page + 21) {
            setItem(41, MenuIcons.nextPageButton(player, page));
        }

        if (page > 0) {
            setItem(39, MenuIcons.prevPageButton(player, page));
        }

        if (waystone != null && (player.hasPermission("waystonesplus.menu.settings") || player.isOp())
                && waystone.getOwnerId().equals(player.getUniqueId().toString())) {
            setItem(40, MenuIcons.settingsButton(player, "settings"));
        }

        if (waystone != null) {
            setItem(44, buildInfoItem(player, waystone));
        }
    }

    private ItemStack buildInfoItem(Player player, Waystone waystone) {
        List<String> lore = new ArrayList<>();
        String name = waystone.getName() != null ? waystone.getName() : "?";
        lore.add(ColoredText.getText(LangManager.teleportInfoName.replace("%name%", name)));
        lore.addAll(waystoneDetailLore(waystone));
        return MenuIcons.infoButton(player, lore);
    }

    private static List<String> waystoneDetailLore(Waystone waystone) {
        String ownerName = resolveOwnerName(waystone.getOwnerId());
        Location loc = waystone.getLocation();
        String worldName = loc != null && loc.getWorld() != null ? loc.getWorld().getName() : "?";
        Visibility visibility = waystone.getVisibility();
        String visibilityName = visibility != null ? visibility.name() : "?";

        int x = loc != null ? loc.getBlockX() : 0;
        int y = loc != null ? loc.getBlockY() : 0;
        int z = loc != null ? loc.getBlockZ() : 0;

        List<String> lore = new ArrayList<>();
        lore.add(ColoredText.getText(LangManager.teleportInfoOwner.replace("%owner%", ownerName)));
        lore.add(ColoredText.getText(LangManager.teleportInfoVisibility.replace("%visibility%", visibilityName)));
        lore.add(ColoredText.getText(LangManager.teleportInfoWorld.replace("%world%", worldName)));
        lore.add(ColoredText.getText(LangManager.teleportInfoCoords
                .replace("%x%", String.valueOf(x))
                .replace("%y%", String.valueOf(y))
                .replace("%z%", String.valueOf(z))));
        return lore;
    }

    private static String resolveOwnerName(String ownerId) {
        if (ownerId == null || ownerId.isEmpty()) {
            return "Unknown";
        }
        try {
            org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(java.util.UUID.fromString(ownerId));
            String name = offline.getName();
            return name != null ? name : ownerId;
        } catch (IllegalArgumentException e) {
            return ownerId;
        }
    }

    private ItemStack buildTabItem(Player player, WaystoneListFilter tabFilter, WaystoneListFilter defaultFilter) {
        boolean selected = tabFilter == filter;
        boolean isDefault = tabFilter == defaultFilter;
        boolean usePackIcons = ResourcePackManager.hasPack(player);

        String baseName = tabDisplayName(tabFilter);
        String name;
        if (selected) {
            // Name formatting is the most reliable selected indicator across item types;
            // glint is kept as a secondary cue where it shows up.
            String plain = org.bukkit.ChatColor.stripColor(ColoredText.getText(baseName));
            name = "&a&l» " + plain + " «";
        } else {
            name = baseName;
        }
        if (isDefault) {
            name = "&e★ " + name;
        }

        List<String> lore = new ArrayList<>();
        if (selected) {
            lore.add(ColoredText.getText(LangManager.teleportTabSelected));
        } else {
            lore.add(ColoredText.getText(LangManager.teleportTabClick));
        }
        lore.add(ColoredText.getText(LangManager.teleportTabShiftDefault));
        if (isDefault) {
            lore.add(ColoredText.getText(LangManager.teleportTabIsDefault));
        }

        return new ItemBuilder(usePackIcons ? Material.PAPER : tabFilter.getMaterial())
                .displayName(ColoredText.getText(name))
                .lore(lore)
                .glow(selected && !usePackIcons)
                .itemModel(usePackIcons ? tabFilter.getItemModel(selected) : null)
                .persistentData("action", "filterTab")
                .persistentData("filter", tabFilter.name())
                .build();
    }

    private static String tabDisplayName(WaystoneListFilter tabFilter) {
        return switch (tabFilter) {
            case GLOBAL -> LangManager.teleportTabGlobal;
            case OWNED -> LangManager.teleportTabOwned;
            case EXPLORED -> LangManager.teleportTabExplored;
            case FAVORITES -> LangManager.teleportTabFavorites;
            case ALL -> LangManager.teleportTabAll;
        };
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event, Waystone waystone) {
        if (event.getInventory() != inventory) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        String action = ItemUtils.getPersistentString(item, "action");
        if (action != null) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, 10, 1);
        }
        handleClick(player, item, event.isShiftClick());
    }

    @Override
    public void handleClick(Player player, ItemStack item) {
        handleClick(player, item, false);
    }

    private void handleClick(Player player, ItemStack item, boolean shiftClick) {
        String action = ItemUtils.getPersistentString(item, "action");
        if (action == null) {
            return;
        }

        switch (action) {
            case "teleport": {
                String waystoneId = ItemUtils.getPersistentString(item, "waystoneId");
                if (shiftClick) {
                    boolean favorited = DB.toggleFavorite(player.getUniqueId().toString(), waystoneId);
                    Waystone target = DB.getWaystone(waystoneId);
                    String name = target != null ? target.getName() : waystoneId;
                    String message = favorited ? LangManager.teleportFavoritedMessage : LangManager.teleportUnfavoritedMessage;
                    player.sendMessage(ColoredText.getText(message.replace("%waystone%", name)));
                    refresh(player);
                } else {
                    teleportToWaystone(player, DB.getWaystone(waystoneId));
                }
                break;
            }
            case "filterTab": {
                WaystoneListFilter selected = WaystoneListFilter.fromString(ItemUtils.getPersistentString(item, "filter"));
                if (shiftClick) {
                    DB.setDefaultTeleportFilter(player.getUniqueId().toString(), selected);
                    player.sendMessage(ColoredText.getText(
                            LangManager.teleportDefaultSet.replace("%filter%", ColoredText.getText(tabDisplayName(selected)))
                    ));
                }
                this.filter = selected;
                this.page = 0;
                refresh(player);
                break;
            }
            case "settings":
                MenuManager.openMenu(player, new SettingsMenu(), this.waystone);
                break;
            case "prevPage": {
                this.page = Integer.parseInt(ItemUtils.getPersistentString(item, "page")) - 1;
                refresh(player);
                break;
            }
            case "nextPage": {
                this.page = Integer.parseInt(ItemUtils.getPersistentString(item, "page")) + 1;
                refresh(player);
                break;
            }
        }
    }

    public void teleportToWaystone(Player player, Waystone waystone) {
        if (waystone != null) {
            boolean adminBypass = player.hasPermission("waystonesplus.command.list") || player.hasPermission("waystonesplus.admin");
            if (!adminBypass && !player.hasPermission("waystonesplus.teleport") && !player.isOp()) {
                player.sendMessage(ColoredText.getText(LangManager.noPermission));
                return;
            }

            if (!adminBypass && waystone.getVisibility() != null && waystone.getVisibility().equals(Visibility.PRIVATE)
                    && !player.hasPermission("waystonesplus.interact.private") && !player.isOp()
                    && waystone.getOwnerId() != null && !waystone.getOwnerId().equals(player.getUniqueId().toString())) {
                player.sendMessage(ColoredText.getText(LangManager.notOwner));
                return;
            }

            player.closeInventory();
            int cooldown = (int) WaystonesPlus.cooldownManager.getRemainingCooldown(player, "waystoneTeleport");
            if (WaystonesPlus.cooldownManager.getRemainingCooldown(player, "waystoneTeleport") > 0
                    && !player.hasPermission("waystonesplus.cooldown.teleport") && !player.isOp()) {
                player.sendMessage(ColoredText.getText("&7You need to wait " + cooldown + " second(s) before teleporting again!"));
                return;
            }

            if (ConfigManager.teleportCountdown > 0 && !player.hasPermission("waystonesplus.countdown.teleport") && !player.isOp()) {
                final int countdownDuration = ConfigManager.teleportCountdown * 20;
                final int countdownInterval = 20;

                AtomicInteger remainingTime = new AtomicInteger(countdownDuration / 20);
                AtomicBoolean countdownRunning = new AtomicBoolean(true);

                BukkitRunnable countdownRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        int time = remainingTime.getAndDecrement();

                        if (time > 0) {
                            player.sendTitle(ColoredText.getText("&7Teleporting in:"), ColoredText.getText("&6" + time));
                        } else {
                            player.resetTitle();
                            Location teleportLoc = DB.calculateTeleportLocation(waystone.getLocation(), waystone.getTeleportDirection());
                            teleportLoc.setWorld(Bukkit.getWorld(waystone.getLocation().getWorld().getName()));
                            player.teleport(teleportLoc);
                            player.sendTitle(ColoredText.getText("&6" + waystone.getName()), null);
                            countdownRunning.set(false);
                            this.cancel();
                        }
                    }
                };

                countdownRunnable.runTaskTimer(WaystonesPlus.getInstance(), 0, countdownInterval);

                Location initialLocation = player.getLocation().getBlock().getLocation();

                Listener moveListener = new Listener() {
                    @EventHandler
                    public void onPlayerMove(PlayerMoveEvent event) {
                        Player movedPlayer = event.getPlayer();
                        Location currentLocation = event.getTo().getBlock().getLocation();

                        if (movedPlayer.equals(player) && countdownRunning.get() && !currentLocation.equals(initialLocation)) {
                            player.resetTitle();
                            player.sendTitle(ColoredText.getText("&cTeleport"), ColoredText.getText("&cCancelled"));
                            countdownRunnable.cancel();
                            countdownRunning.set(false);
                            HandlerList.unregisterAll(this);
                        } else if (!countdownRunning.get()) {
                            HandlerList.unregisterAll(this);
                        }
                    }
                };

                Bukkit.getPluginManager().registerEvents(moveListener, WaystonesPlus.getInstance());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!countdownRunning.get()) {
                            HandlerList.unregisterAll(moveListener);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(WaystonesPlus.getInstance(), 0, 1);
            } else {
                player.resetTitle();
                Location teleportLoc = DB.calculateTeleportLocation(waystone.getLocation(), waystone.getTeleportDirection());
                teleportLoc.setWorld(Bukkit.getWorld(waystone.getLocation().getWorld().getName()));
                player.teleport(teleportLoc);
                player.sendTitle(ColoredText.getText("&6" + waystone.getName()), null);
            }

            if (WaystonesPlus.cooldownManager.getRemainingCooldown(player, "waystoneTeleport") == 0
                    && !player.hasPermission("waystonesplus.cooldown.teleport") && !player.isOp()) {
                WaystonesPlus.cooldownManager.addPlayerCooldown(player, "waystoneTeleport", ConfigManager.waystoneTeleportCooldown);
            }
        }
    }
}
