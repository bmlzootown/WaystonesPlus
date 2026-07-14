package org.sweetrazory.waystonesplus.commands.subcommands;

import org.bukkit.entity.Player;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.menu.submenus.AdminWaystoneListMenu;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.SubCommand;

public class ListAll implements SubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void run(Player player, String[] args) {
        if (!player.hasPermission("waystonesplus.command.list") && !player.isOp()) {
            player.sendMessage(ColoredText.getText(LangManager.noPermission));
            return;
        }
        int page = 0;
        if (args.length > 1) {
            try {
                page = Math.max(0, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        MenuManager.openMenu(player, new AdminWaystoneListMenu(page), null);
    }
}
