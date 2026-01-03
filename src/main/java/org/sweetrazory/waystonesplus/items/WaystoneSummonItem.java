package org.sweetrazory.waystonesplus.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.enums.Visibility;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.memoryhandlers.WaystoneMemory;
import org.sweetrazory.waystonesplus.types.WaystoneType;
import org.sweetrazory.waystonesplus.utils.ColoredText;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class WaystoneSummonItem {
    public static ItemStack getLodestoneHead(@Nullable String name, String type, @Nullable String headOwnerId, @Nullable String texturesString, @NotNull Visibility visibility) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        WaystoneType ws = WaystoneMemory.getWaystoneTypes().get(type);

        String playerId = "9bf98ec7-ca26-45b2-a7f2-976f7655d361";
        String textures = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUwMjE2NTk3ZjE2YmNkYzIyZjEwYTNjYzIyOTljYTg4NGM1N2U0Njg1MGZiOGRlZjAxODk1NjYyZDM5MDQwNCJ9fX0=";

        String profileId = headOwnerId == null ? (ws != null && ws.getSpawnItemHeadId() != null ? ws.getSpawnItemHeadId() : playerId) : headOwnerId;
        String profileTextures = texturesString == null ? (ws != null && ws.getSpawnItemTextures() != null ? ws.getSpawnItemTextures() : textures) : texturesString;

        // Use Paper's PlayerProfile API to set custom skull texture (no warnings!)
        try {
            // Create a PlayerProfile with the UUID
            PlayerProfile profile = Bukkit.createProfile(UUID.fromString(profileId));
            
            // Decode the base64 texture string to extract the URL
            String decodedTextures = new String(Base64.getDecoder().decode(profileTextures));
            String textureUrl = extractTextureUrl(decodedTextures);
            
            if (textureUrl != null) {
                // Get PlayerTextures and set the skin URL
                PlayerTextures playerTextures = profile.getTextures();
                URL skinUrl = new URI(textureUrl).toURL();
                playerTextures.setSkin(skinUrl);
                profile.setTextures(playerTextures);
            }
            
            // Set the profile on SkullMeta using the proper API (no reflection needed!)
            skullMeta.setOwnerProfile(profile);
        } catch (URISyntaxException | MalformedURLException e) {
            WaystonesPlus.Logger().warning("Invalid texture URL: " + e.getMessage());
        } catch (Exception e) {
            WaystonesPlus.Logger().warning("Failed to set custom skull texture: " + e.getMessage());
            e.printStackTrace();
        }

        skullItem.setItemMeta(skullMeta);
        ItemMeta itemMeta = skullItem.getItemMeta();
        itemMeta.setDisplayName(name != null && !name.isEmpty() ?
                ColoredText.getText(name) :
                ColoredText.getText(LangManager.newWaystoneName));

        NamespacedKey waystoneType = new NamespacedKey(WaystonesPlus.getInstance(), "waystoneType");
        NamespacedKey waystoneVisibility = new NamespacedKey(WaystonesPlus.getInstance(), "waystoneVisibility");
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(waystoneType, PersistentDataType.STRING, type);
        dataContainer.set(waystoneVisibility, PersistentDataType.STRING, visibility.name());
        itemMeta.setLore(new ArrayList<String>() {{
            add(ColoredText.getText(visibility == Visibility.PRIVATE ? "&cPRIVATE" : visibility == Visibility.PUBLIC ? "&2PUBLIC" : "&eGLOBAL"));
        }});
        skullItem.setItemMeta(itemMeta);

        return skullItem;
    }

    /**
     * Extracts the texture URL from a decoded base64 texture JSON string.
     * Format: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/..."}}}
     */
    private static String extractTextureUrl(String decodedJson) {
        try {
            // Simple extraction: find the URL in the JSON
            int urlIndex = decodedJson.indexOf("\"url\":\"");
            if (urlIndex == -1) return null;
            
            urlIndex += 7; // Skip past "url":"
            int urlEnd = decodedJson.indexOf("\"", urlIndex);
            if (urlEnd == -1) return null;
            
            return decodedJson.substring(urlIndex, urlEnd);
        } catch (Exception e) {
            return null;
        }
    }
}