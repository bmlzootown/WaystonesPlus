package org.sweetrazory.waystonesplus.menu.submenus;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.Menu;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.ItemBuilder;
import org.sweetrazory.waystonesplus.utils.ItemUtils;
import org.sweetrazory.waystonesplus.utils.MenuIcons;
import org.sweetrazory.waystonesplus.utils.WaystoneParticleOptions;
import org.sweetrazory.waystonesplus.waystone.Waystone;

import java.util.List;

public class ParticleMenu extends Menu {
    private final WaystoneParticleOptions.Option[] options = WaystoneParticleOptions.OPTIONS;

    public ParticleMenu() {
        this(0);
    }

    public ParticleMenu(int page) {
        super(54, ColoredText.getText(LangManager.particleMenuTitle), page);
    }

    @Override
    public void initializeItems(Player player, Waystone waystone) {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName(" ").build();
        inventory.setContents(new ItemStack[]{
                filler, filler, filler, filler, filler, filler, filler, filler, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, null, null, null, null, null, null, null, filler,
                filler, filler, filler, filler, filler, null, filler, filler, filler
        });

        setItem(49, MenuIcons.returnButton(player, "menu"));
        setItem(45, new ItemBuilder(Material.BARRIER)
                .displayName(ColoredText.getText("&cTurn Off Particles"))
                .persistentData("action", "offParticle")
                .build());

        int pageStart = page * WaystoneParticleOptions.PAGE_SIZE;
        if (pageStart + WaystoneParticleOptions.PAGE_SIZE < options.length) {
            setItem(50, MenuIcons.nextPageButton(player));
        }
        if (page > 0) {
            setItem(48, MenuIcons.prevPageButton(player));
        }

        Particle current = waystone.getParticle();
        int k = 0;
        for (int row = 1; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                int index = pageStart + k;
                if (index >= options.length) {
                    break;
                }
                WaystoneParticleOptions.Option option = options[index];
                boolean selected = option.particle() == current;
                ItemBuilder builder = new ItemBuilder(option.icon())
                        .displayName(ColoredText.getText((selected ? "&8" : "&6") + option.displayName()))
                        .persistentData("action", "setParticle")
                        .persistentData("particle", option.particle().name());
                if (selected) {
                    builder.lore(List.of(ColoredText.getText("&8Currently active")));
                }
                setItem(row * 9 + col + 1, builder.build());
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

        switch (action) {
            case "menu" -> MenuManager.openMenu(player, new SettingsMenu(), waystone);
            case "nextPage" -> {
                page++;
                refresh(player, waystone);
            }
            case "prevPage" -> {
                page--;
                refresh(player, waystone);
            }
            case "offParticle" -> {
                waystone.setParticle(null);
                refresh(player, waystone);
            }
            case "setParticle" -> {
                String particleName = ItemUtils.getPersistentString(item, "particle");
                if (particleName != null) {
                    waystone.setParticle(Particle.valueOf(particleName));
                    refresh(player, waystone);
                }
            }
        }
    }
}
