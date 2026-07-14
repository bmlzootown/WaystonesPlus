package org.sweetrazory.waystonesplus.memoryhandlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.sweetrazory.waystonesplus.WaystonesPlus;
import org.sweetrazory.waystonesplus.enums.Visibility;
import org.sweetrazory.waystonesplus.utils.DB;
import org.sweetrazory.waystonesplus.waystone.Waystone;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    public static Connection connection;

    public static ResultSet execute(String query, Object... parameters) {
        // Note: Caller is responsible for closing the ResultSet and PreparedStatement
        // This method should ideally be refactored to use try-with-resources, but that would require
        // significant changes to all callers. For now, this is a known limitation.
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            setParameters(statement, parameters);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    public static int executeUpdate(String query, Object... parameters) {
        int rowsAffected = 0;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            setParameters(statement, parameters);
            rowsAffected = statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }

    public static void removeWaystone(String waystoneId) {
        try {
            String deleteWaystoneQuery = "DELETE FROM waystones WHERE id = ?";
            String deleteExploredWaystonesQuery = "DELETE FROM explored_waystones WHERE waystoneId = ?";
            String deleteFavoriteWaystonesQuery = "DELETE FROM favorite_waystones WHERE waystoneId = ?";

            PreparedStatement deleteWaystoneStatement = connection.prepareStatement(deleteWaystoneQuery);
            deleteWaystoneStatement.setString(1, waystoneId);
            deleteWaystoneStatement.executeUpdate();
            deleteWaystoneStatement.close();

            PreparedStatement deleteExploredWaystonesStatement = connection.prepareStatement(deleteExploredWaystonesQuery);
            deleteExploredWaystonesStatement.setString(1, waystoneId);
            deleteExploredWaystonesStatement.executeUpdate();
            deleteExploredWaystonesStatement.close();

            PreparedStatement deleteFavoriteWaystonesStatement = connection.prepareStatement(deleteFavoriteWaystonesQuery);
            deleteFavoriteWaystonesStatement.setString(1, waystoneId);
            deleteFavoriteWaystonesStatement.executeUpdate();
            deleteFavoriteWaystonesStatement.close();

            WaystonesPlus.Logger().info("Waystone removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");

            String databasePath = WaystonesPlus.getInstance().getDataFolder() + "/database/database.db";

            File databaseFile = new File(databasePath);
            File parentDirectory = databaseFile.getParentFile();

            if (parentDirectory != null && !parentDirectory.exists()) {
                if (!parentDirectory.mkdirs()) {
                    WaystonesPlus.Logger().info("Failed to create database directory.");
                    return;
                }
            }

            if (!databaseFile.exists()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                createWaystonesTable();
                createExploredWaystonesTable();
                createFavoriteWaystonesTable();
                createPlayerPreferencesTable();
                migrateTeleportLocationColumns();
                WaystonesPlus.Logger().info("Database created and initialized successfully.");
            } else {
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                createFavoriteWaystonesTable();
                createPlayerPreferencesTable();
                migrateTeleportLocationColumns();
                WaystonesPlus.Logger().info("Database initialized successfully.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void migrateWaystones() {
        File pluginFolder = new File(WaystonesPlus.getInstance().getDataFolder().getAbsolutePath());

        File waystonesFolder = new File(pluginFolder, "waystones");
        if (!waystonesFolder.exists() || !waystonesFolder.isDirectory()) {
            WaystonesPlus.Logger().info("No Waystones to be migrate.");
            return;
        }

        List<Waystone> waystones = new ArrayList<>();

        findConfigFiles(waystonesFolder, waystones);

        for (Waystone waystone : waystones) {
            WaystonesPlus.Logger().info("Parsed Waystone: " + waystone.getId());
            DB.insertWaystone(waystone);
        }

        if (!waystones.isEmpty()) {
            try {
                deleteDirectory(waystonesFolder);
                WaystonesPlus.Logger().info("Deleted 'waystones' folder.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void findConfigFiles(File directory, List<Waystone> waystones) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals("config.yml")) {
                    WaystonesPlus.Logger().info("Found 'config.yml' file");
                    Waystone waystone = parseConfigYaml(file);
                    if (waystone != null) {
                        waystones.add(waystone);
                    }
                } else if (file.isDirectory()) {
                    findConfigFiles(file, waystones);
                }
            }
        }
    }

    private Waystone parseConfigYaml(File configFile) {
        try (FileInputStream fis = new FileInputStream(configFile);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(isr);

            String owner = (String) config.get("owner");
            String visibility = (String) config.get("visibility");
            String name = (String) config.getOrDefault("name", LangManager.newWaystoneName);
            name = name.replaceAll("\uFFFD", "&");

            Map<String, Double> locationMap = (Map<String, Double>) config.get("location");
            double x = locationMap.get("x");
            double y = locationMap.get("y");
            double z = locationMap.get("z");

            String id = (String) config.get("id");
            String type = (String) config.get("type");
            String world = (String) config.getOrDefault("location.world", Bukkit.getServer().getWorlds().get(0).getName());

            List<String> entityIdStrings = (List<String>) config.get("entityIds");
            List<Integer> entityIds = new ArrayList<>();
            for (String entityIdString : entityIdStrings) {
                int entityId = Integer.parseInt(entityIdString);
                entityIds.add(entityId);
            }
            // Create and return the Waystone object
            // Default teleport direction for migrated waystones (default to North)
            Location waystoneLocation = new Location(Bukkit.getWorld(world), x, y, z);
            Waystone waystone = new Waystone(id, name, waystoneLocation, type, owner, Particle.ENCHANT, Visibility.fromString(visibility), entityIds, Material.LODESTONE, "N");
            return waystone;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void createWaystonesTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS waystones (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "location TEXT NOT NULL, " +
                "entityIds TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "owner TEXT NOT NULL, " +
                "icon TEXT NOT NULL, " +
                "visibility TEXT NOT NULL, " +
                "particle TEXT, " +
                "teleportDirection TEXT DEFAULT 'N'" +
                ")";

        try {
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createExploredWaystonesTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS explored_waystones (playerId TEXT NOT NULL, playerName TEXT NOT NULL, waystoneId TEXT NOT NULL);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createFavoriteWaystonesTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS favorite_waystones (" +
                "playerId TEXT NOT NULL, " +
                "waystoneId TEXT NOT NULL, " +
                "PRIMARY KEY (playerId, waystoneId)" +
                ")";
        try {
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createPlayerPreferencesTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS player_preferences (" +
                "playerId TEXT PRIMARY KEY, " +
                "defaultTeleportFilter TEXT NOT NULL DEFAULT 'ALL'" +
                ")";
        try {
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void migrateTeleportLocationColumns() {
        try {
            // Check if columns exist
            Statement checkStatement = connection.createStatement();
            ResultSet columns = checkStatement.executeQuery("PRAGMA table_info(waystones)");
            boolean hasTeleportDirection = false;
            boolean hasTeleportLocation = false;
            
            while (columns.next()) {
                String columnName = columns.getString("name");
                if ("teleportDirection".equals(columnName)) {
                    hasTeleportDirection = true;
                }
                if ("teleportLocation".equals(columnName)) {
                    hasTeleportLocation = true;
                }
            }
            columns.close();
            checkStatement.close();
            
            // Add teleportDirection column if it doesn't exist
            if (!hasTeleportDirection) {
                Statement alterStatement = connection.createStatement();
                alterStatement.execute("ALTER TABLE waystones ADD COLUMN teleportDirection TEXT DEFAULT 'N'");
                alterStatement.close();
                WaystonesPlus.Logger().info("Added teleportDirection column to waystones table.");
            }
            
            // Migrate old teleportLocation/teleportYaw data to teleportDirection
            if (hasTeleportLocation && !hasTeleportDirection) {
                Statement updateStatement = connection.createStatement();
                ResultSet waystones = updateStatement.executeQuery("SELECT id, location, teleportLocation FROM waystones WHERE teleportLocation IS NOT NULL");
                
                PreparedStatement updateDirection = connection.prepareStatement("UPDATE waystones SET teleportDirection = ? WHERE id = ?");
                
                while (waystones.next()) {
                    String id = waystones.getString("id");
                    String locationString = waystones.getString("location");
                    String teleportLocationString = waystones.getString("teleportLocation");
                    
                    // Parse waystone location
                    String[] locationParts = locationString.split(",");
                    if (locationParts.length == 4) {
                        double waystoneX = Double.parseDouble(locationParts[1].split("=")[1]);
                        double waystoneZ = Double.parseDouble(locationParts[2].split("=")[1]);
                        double waystoneCenterX = waystoneX + 0.5;
                        double waystoneCenterZ = waystoneZ + 0.5;
                        
                        // Parse teleport location
                        String[] teleportParts = teleportLocationString.split(",");
                        if (teleportParts.length >= 4) {
                            double teleportX = Double.parseDouble(teleportParts[1].split("=")[1]);
                            double teleportZ = Double.parseDouble(teleportParts[2].split("=")[1]);
                            
                            // Convert to cardinal direction
                            String direction = DB.positionToCardinalDirection(teleportX, teleportZ, waystoneCenterX, waystoneCenterZ);
                            updateDirection.setString(1, direction);
                            updateDirection.setString(2, id);
                            updateDirection.executeUpdate();
                        }
                    }
                }
                
                waystones.close();
                updateStatement.close();
                updateDirection.close();
                WaystonesPlus.Logger().info("Migrated teleport locations to cardinal directions.");
            } else if (!hasTeleportLocation && !hasTeleportDirection) {
                // No old data, just set default direction for all waystones
                Statement updateStatement = connection.createStatement();
                updateStatement.execute("UPDATE waystones SET teleportDirection = 'N' WHERE teleportDirection IS NULL");
                updateStatement.close();
                WaystonesPlus.Logger().info("Set default teleport direction for existing waystones.");
            }
        } catch (SQLException e) {
            WaystonesPlus.Logger().warning("Error migrating teleport location columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                WaystonesPlus.Logger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!directory.delete()) {
                throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
            }
        }
    }
}
