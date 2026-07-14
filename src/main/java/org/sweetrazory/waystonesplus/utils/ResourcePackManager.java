package org.sweetrazory.waystonesplus.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.memoryhandlers.ConfigManager;
import org.sweetrazory.waystonesplus.memoryhandlers.LangManager;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.menu.TeleportMenu;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bundles, hosts, and applies the WaystonesPlus UI resource pack for custom tab icons.
 */
public class ResourcePackManager implements Listener {
    private static final UUID LEGACY_PACK_UUID = UUID.fromString("a1b2c3d4-e5f6-4789-a012-3456789abcde");

    private static ResourcePackManager instance;

    private final Set<UUID> playersWithPack = ConcurrentHashMap.newKeySet();
    /** Players who already got the decline reminder this session. */
    private final Set<UUID> declineReminderSent = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, UUID> pendingPackIds = new ConcurrentHashMap<>();
    /** Hostname the client used to connect (from PlayerLoginEvent), without port. */
    private final ConcurrentHashMap<UUID, String> connectHosts = new ConcurrentHashMap<>();

    private Path packZip;
    private byte[] packHash;
    private String packHashHex;
    private HttpServer httpServer;
    private int packPort;

    public static ResourcePackManager getInstance() {
        return instance;
    }

    public static boolean hasPack(Player player) {
        return instance != null && instance.playersWithPack.contains(player.getUniqueId());
    }

    public void enable() {
        instance = this;
        if (!ConfigManager.enableResourcePack) {
            WaystonesPlus.Logger().info("UI resource pack is disabled in config.");
            return;
        }

        try {
            preparePackZip();
            startHttpServer();
            Bukkit.getPluginManager().registerEvents(this, WaystonesPlus.getInstance());
            WaystonesPlus.Logger().info("UI resource pack HTTP server listening on port " + packPort
                    + " (example URL: http://127.0.0.1:" + packPort + "/waystonesplus-ui.zip)");
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendPack(player);
            }
        } catch (Exception e) {
            WaystonesPlus.Logger().severe("Failed to start UI resource pack hosting:");
            e.printStackTrace();
        }
    }

    public void disable() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        playersWithPack.clear();
        pendingPackIds.clear();
        declineReminderSent.clear();
        connectHosts.clear();
        instance = null;
    }

    public void sendPack(Player player) {
        offerPack(player, false);
    }

    public void resendPack(Player player) {
        offerPack(player, true);
    }

    private void offerPack(Player player, boolean clearPrevious) {
        if (!ConfigManager.enableResourcePack || packHashHex == null || httpServer == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        playersWithPack.remove(playerId);

        if (clearPrevious) {
            UUID previous = pendingPackIds.remove(playerId);
            player.clearResourcePacks();
            if (previous != null) {
                player.removeResourcePack(previous);
            }
            player.removeResourcePack(LEGACY_PACK_UUID);
        }

        long delay = clearPrevious ? 15L : 1L;
        Bukkit.getScheduler().runTaskLater(WaystonesPlus.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }

            UUID packId = UUID.randomUUID();
            pendingPackIds.put(playerId, packId);
            String url = buildPackUrl(player, packId);

            try {
                ResourcePackInfo info = ResourcePackInfo.resourcePackInfo()
                        .id(packId)
                        .uri(URI.create(url))
                        .hash(packHashHex)
                        .build();

                ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                        .packs(info)
                        .prompt(Component.text("WaystonesPlus Menu Icons"))
                        .required(ConfigManager.resourcePackRequired)
                        .replace(true)
                        .build();

                player.sendResourcePacks(request);
                WaystonesPlus.Logger().info("Offered UI resource pack to " + player.getName() + " url=" + url);

                if (clearPrevious) {
                    player.sendMessage(Component.text("If no prompt appears: Multiplayer → select this server → Edit → set ", NamedTextColor.GRAY)
                            .append(Component.text("Server Resource Packs", NamedTextColor.YELLOW))
                            .append(Component.text(" to ", NamedTextColor.GRAY))
                            .append(Component.text("Prompt", NamedTextColor.YELLOW))
                            .append(Component.text(" or ", NamedTextColor.GRAY))
                            .append(Component.text("Enabled", NamedTextColor.YELLOW))
                            .append(Component.text(".", NamedTextColor.GRAY)));
                    player.sendMessage(Component.text("Test download link: ", NamedTextColor.GRAY)
                            .append(Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.openUrl(url))));
                }
            } catch (Exception e) {
                WaystonesPlus.Logger().severe("Failed to offer resource pack to " + player.getName() + ":");
                e.printStackTrace();
                player.sendMessage(ColoredText.getText("&cFailed to offer the resource pack. Check server logs."));
            }
        }, delay);
    }

    private String buildPackUrl(Player player, UUID packId) {
        String host = ConfigManager.resourcePackHost;
        if (host == null || host.isBlank()) {
            host = connectHosts.get(player.getUniqueId());
        }
        if (host == null || host.isBlank()) {
            host = resolvePublicHost();
        }
        // Strip port if login hostname included one (e.g. "localhost:25565")
        int colon = host.indexOf(':');
        if (colon > 0 && host.indexOf(']') < 0) {
            host = host.substring(0, colon);
        }
        return "http://" + host + ":" + packPort + "/waystonesplus-ui.zip?v=" + packId;
    }

    public boolean isReady() {
        return ConfigManager.enableResourcePack && packHashHex != null && httpServer != null;
    }

    /**
     * One decline reminder per join — not on menu open / repeated joins of the same pack status.
     */
    private void sendDeclineReminderOnce(Player player) {
        UUID playerId = player.getUniqueId();
        if (!declineReminderSent.add(playerId)) {
            return;
        }
        player.sendMessage(ColoredText.getText(LangManager.resourcePackDeclinedReminder));
        player.sendMessage(ColoredText.getText(LangManager.resourcePackDeclinedHowTo));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String hostname = event.getHostname();
        if (hostname != null && !hostname.isBlank()) {
            connectHosts.put(event.getPlayer().getUniqueId(), hostname);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!ConfigManager.enableResourcePack) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(WaystonesPlus.getInstance(), () -> sendPack(event.getPlayer()), 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playersWithPack.remove(playerId);
        declineReminderSent.remove(playerId);
        pendingPackIds.remove(playerId);
        connectHosts.remove(playerId);
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        UUID expected = pendingPackIds.get(playerId);
        if (expected != null && !expected.equals(event.getID())) {
            // Ignore stale offers
            return;
        }
        Player player = event.getPlayer();
        WaystonesPlus.Logger().info("Resource pack status for " + player.getName()
                + ": " + event.getStatus() + " id=" + event.getID());

        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED -> {
                playersWithPack.add(playerId);
                if (MenuManager.hasOpenMenu(player) && MenuManager.getOpenMenu(player) instanceof TeleportMenu teleportMenu) {
                    teleportMenu.refreshPublic(player);
                }
                player.sendMessage(ColoredText.getText("&aWaystonesPlus menu icons loaded."));
            }
            case DECLINED -> {
                playersWithPack.remove(playerId);
                sendDeclineReminderOnce(player);
            }
            case FAILED_DOWNLOAD, INVALID_URL -> {
                playersWithPack.remove(playerId);
                String url = buildPackUrl(player, expected != null ? expected : UUID.randomUUID());
                player.sendMessage(Component.text("WaystonesPlus pack download failed. ", NamedTextColor.RED)
                        .append(Component.text("Open this link to verify the HTTP server: ", NamedTextColor.GRAY))
                        .append(Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(url))));
                player.sendMessage(Component.text("Then set ", NamedTextColor.GRAY)
                        .append(Component.text("resource-pack-host", NamedTextColor.YELLOW))
                        .append(Component.text(" in config.yml to an address your client can reach.", NamedTextColor.GRAY)));
            }
            case FAILED_RELOAD, DISCARDED -> playersWithPack.remove(playerId);
            default -> {
            }
        }
    }

    private void preparePackZip() throws Exception {
        Path dataFolder = WaystonesPlus.getInstance().getDataFolder().toPath();
        Path packDir = dataFolder.resolve("resourcepack");
        Files.createDirectories(packDir);

        extractBundledPack(packDir);

        packZip = dataFolder.resolve("waystonesplus-ui.zip");
        zipDirectory(packDir, packZip);
        packHash = sha1(packZip);
        packHashHex = toHex(packHash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }

    private void extractBundledPack(Path targetDir) throws IOException {
        if (Files.exists(targetDir)) {
            Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(targetDir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        Files.createDirectories(targetDir);

        URL codeSource = WaystonesPlus.class.getProtectionDomain().getCodeSource().getLocation();
        Path jarPath;
        try {
            jarPath = Path.of(codeSource.toURI());
        } catch (URISyntaxException e) {
            jarPath = Path.of(codeSource.getPath());
        }

        if (Files.isDirectory(jarPath)) {
            Path devRoot = jarPath.resolve("resourcepack");
            if (Files.isDirectory(devRoot)) {
                copyDirectory(devRoot, targetDir);
                return;
            }
        }

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("resourcepack/") || name.equals("resourcepack/")) {
                    continue;
                }
                String relative = name.substring("resourcepack/".length());
                if (relative.isEmpty()) {
                    continue;
                }
                Path out = targetDir.resolve(relative);
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    try (InputStream in = jar.getInputStream(entry)) {
                        Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Could not extract bundled resource pack from " + jarPath, e);
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourceDir.relativize(file).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private byte[] sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return digest.digest();
    }

    private void startHttpServer() throws IOException {
        packPort = ConfigManager.resourcePackPort;
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", packPort), 0);
        httpServer.createContext("/waystonesplus-ui.zip", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            byte[] bytes = Files.readAllBytes(packZip);
            exchange.getResponseHeaders().add("Content-Type", "application/zip");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        httpServer.setExecutor(null);
        httpServer.start();
    }

    private String resolvePublicHost() {
        String configured = Bukkit.getIp();
        if (configured != null && !configured.isBlank() && !configured.equals("0.0.0.0")) {
            return configured;
        }
        return "127.0.0.1";
    }
}
