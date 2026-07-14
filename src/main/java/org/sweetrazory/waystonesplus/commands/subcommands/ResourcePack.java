package org.sweetrazory.waystonesplus.commands.subcommands;

import org.bukkit.entity.Player;
import org.sweetrazory.waystonesplus.memoryhandlers.ConfigManager;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.ResourcePackManager;
import org.sweetrazory.waystonesplus.utils.SubCommand;

public class ResourcePack implements SubCommand {
    private final String name;

    public ResourcePack() {
        this("resourcepack");
    }

    public ResourcePack(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run(Player player, String[] args) {
        if (!ConfigManager.enableResourcePack) {
            player.sendMessage(ColoredText.getText(LangManager.resourcePackDisabled));
            return;
        }

        ResourcePackManager manager = ResourcePackManager.getInstance();
        if (manager == null || !manager.isReady()) {
            player.sendMessage(ColoredText.getText(LangManager.resourcePackUnavailable));
            return;
        }

        if (ResourcePackManager.hasPack(player)) {
            player.sendMessage(ColoredText.getText(LangManager.resourcePackAlreadyLoaded));
            return;
        }

        player.sendMessage(ColoredText.getText(LangManager.resourcePackSending));
        manager.resendPack(player);
    }
}
