package org.sweetrazory.waystonesplus.commands.subcommands;

import org.bukkit.entity.Player;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class Help implements SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run(Player player, String[] args) {
        List<String> helpLines = new ArrayList<>();
        helpLines.add(ColoredText.getText("&4--------[ &6WaystonesPlus Help &4]--------"));
        
        // Get command
        if (player.hasPermission("waystonesplus.command.get") || player.isOp()) {
            helpLines.add(ColoredText.getText("&6/wsp get <type> [name]"));
            helpLines.add(ColoredText.getText("&7  Gives you a Waystone item of the specified type."));
            helpLines.add(ColoredText.getText("&7  Optional: Add a custom name for the waystone."));
            helpLines.add("");
        }
        
        // Rename command
        if (player.hasPermission("waystonesplus.command.rename") || player.isOp()) {
            helpLines.add(ColoredText.getText("&6/wsp rename <name>"));
            helpLines.add(ColoredText.getText("&7  Renames the waystone item you're holding."));
            helpLines.add(ColoredText.getText("&7  Hold a waystone item in your hand to rename it."));
            helpLines.add("");
        }
        
        // SetVisibility command
        if (player.hasPermission("waystonesplus.command.visibility") || player.isOp()) {
            helpLines.add(ColoredText.getText("&6/wsp setvisibility <visibility>"));
            helpLines.add(ColoredText.getText("&7  Changes the visibility of the waystone item you're holding."));
            helpLines.add(ColoredText.getText("&7  Options: &cPRIVATE &7| &aPUBLIC &7| &eGLOBAL"));
            helpLines.add("");
        }
        
        // Reload command
        if (player.hasPermission("waystonesplus.command.reload") || player.isOp()) {
            helpLines.add(ColoredText.getText("&6/wsp reload"));
            helpLines.add(ColoredText.getText("&7  Reloads the plugin configuration and waystones."));
            helpLines.add("");
        }
        
        // Help command (always show)
        helpLines.add(ColoredText.getText("&6/wsp help"));
        helpLines.add(ColoredText.getText("&7  Shows this help message."));
        helpLines.add("");

        helpLines.add(ColoredText.getText("&6/wsp resourcepack &7(or &6/wsp pack&7)"));
        helpLines.add(ColoredText.getText("&7  Re-prompts you for the WaystonesPlus menu icon pack"));
        helpLines.add(ColoredText.getText("&7  (useful if you previously declined it)."));
        helpLines.add("");
        
        // General usage instructions
        helpLines.add(ColoredText.getText("&e--- Usage Guide ---"));
        helpLines.add(ColoredText.getText("&7To rename a waystone:"));
        helpLines.add(ColoredText.getText("&7  - Use an anvil. Costs 1 level of experience."));
        helpLines.add(ColoredText.getText("&7To use a waystone:"));
        helpLines.add(ColoredText.getText("&7  - Right-click a placed waystone to open"));
        helpLines.add(ColoredText.getText("&7    the teleportation menu."));
        helpLines.add(ColoredText.getText("&4-----------------------------------"));
        
        // Send all help lines
        for (String line : helpLines) {
            player.sendMessage(line);
        }
    }
}
