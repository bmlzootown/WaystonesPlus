package org.sweetrazory.waystonesplus.utils;

import org.bukkit.*;
import org.sweetrazory.waystonesplus.enums.Visibility;
import org.sweetrazory.waystonesplus.enums.WaystoneListFilter;
import org.sweetrazory.waystonesplus.memoryhandlers.DatabaseManager;
import org.sweetrazory.waystonesplus.memoryhandlers.WaystoneMemory;
import org.sweetrazory.waystonesplus.waystone.Waystone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DB {
    public static List<WaystoneParticleInfo> getAllWaystoneInfo() {
        List<WaystoneParticleInfo> waystoneInfoList = new ArrayList<>();
        String query = "SELECT id, particle, location FROM waystones";
        ResultSet resultSet = DatabaseManager.execute(query);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                String uuid = resultSet.getString("id");
                String particleString = resultSet.getString("particle");
                String locationString = resultSet.getString("location");

                Particle particle = Particles.parse(particleString);
                Location location = parseLocationString(locationString);

                    WaystoneParticleInfo waystoneInfo = new WaystoneParticleInfo(uuid, particle, location);
                    waystoneInfoList.add(waystoneInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystoneInfoList;
    }

    private static Location parseLocationString(String locationString) {
        // Parse JSON manually
        String[] locationParts = locationString.split(",");
        String worldName = locationParts[0].split("=")[1];
        double x = Double.parseDouble(locationParts[1].split("=")[1]);
        double y = Double.parseDouble(locationParts[2].split("=")[1]);
        double z = Double.parseDouble(locationParts[3].split("=")[1]);

        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            return new Location(world, x, y, z);
        } else {
            // Handle if the world does not exist
            return null;
        }
    }


    public static List<Waystone> getPlayerWaystones(String playerId) {
        List<Waystone> waystones = new ArrayList<>();
        String query = "SELECT * FROM waystones WHERE owner = ?";
        ResultSet resultSet = DatabaseManager.execute(query, playerId);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                String uuid = resultSet.getString("id");
                String name = resultSet.getString("name");

                String locationObject = resultSet.getString("location");
                // Parse JSON manually
                String[] locationParts = locationObject.split(",");
                String worldName = locationParts[0].split("=")[1];
                double x = Double.parseDouble(locationParts[1].split("=")[1]);
                double y = Double.parseDouble(locationParts[2].split("=")[1]);
                double z = Double.parseDouble(locationParts[3].split("=")[1]);

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                String entityIds = resultSet.getString("entityIds");
                String type = resultSet.getString("type");
                String owner = resultSet.getString("owner");
                String particle = resultSet.getString("particle");
                String visibility = resultSet.getString("visibility");
                Material icon = Material.matchMaterial(resultSet.getString("icon"));

                String[] entityIdStrings = entityIds.split(",");
                Integer[] entityIdsArray = new Integer[entityIdStrings.length];
                for (int i = 0; i < entityIdStrings.length; i++) {
                    entityIdsArray[i] = Integer.parseInt(entityIdStrings[i]);
                }

                    String teleportDirection = parseTeleportDirection(resultSet, location);
                    Waystone waystone = new Waystone(uuid, name, location, type, owner, Particles.parse(particle), Visibility.fromString(visibility), Arrays.asList(entityIdsArray), icon, teleportDirection);
                    waystones.add(waystone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystones;
    }

    public static List<Waystone> getGlobalAndPublicWaystones() {
        List<Waystone> waystones = new ArrayList<>();
        String query = "SELECT * FROM waystones WHERE visibility IN ('GLOBAL', 'PUBLIC')";
        ResultSet resultSet = DatabaseManager.execute(query);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                String uuid = resultSet.getString("id");
                String name = resultSet.getString("name");

                String locationObject = resultSet.getString("location");
                // Parse JSON manually
                String[] locationParts = locationObject.split(",");
                String worldName = locationParts[0].split("=")[1];
                double x = Double.parseDouble(locationParts[1].split("=")[1]);
                double y = Double.parseDouble(locationParts[2].split("=")[1]);
                double z = Double.parseDouble(locationParts[3].split("=")[1]);

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                String entityIds = resultSet.getString("entityIds");
                String type = resultSet.getString("type");
                String owner = resultSet.getString("owner");
                String particle = resultSet.getString("particle");
                String visibility = resultSet.getString("visibility");
                Material icon = Material.matchMaterial(resultSet.getString("icon"));
                String[] entityIdStrings = entityIds.split(",");
                Integer[] entityIdsArray = new Integer[entityIdStrings.length];
                for (int i = 0; i < entityIdStrings.length; i++) {
                    entityIdsArray[i] = Integer.parseInt(entityIdStrings[i]);
                }

                    String teleportDirection = parseTeleportDirection(resultSet, location);
                    Waystone waystone = new Waystone(uuid, name, location, type, owner, Particles.parse(particle), Visibility.fromString(visibility), Arrays.asList(entityIdsArray), icon, teleportDirection);
                    waystones.add(waystone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystones;
    }

    public static List<Waystone> getAllWaystones() {
        List<Waystone> waystones = new ArrayList<>();
        String query = "SELECT * FROM waystones";
        ResultSet resultSet = DatabaseManager.execute(query);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    String uuid = resultSet.getString("id");
                    String name = resultSet.getString("name");

                    String locationObject = resultSet.getString("location");
                    // Parse JSON manually
                    String[] locationParts = locationObject.split(",");
                    String worldName = locationParts[0].split("=")[1];
                    double x = Double.parseDouble(locationParts[1].split("=")[1]);
                    double y = Double.parseDouble(locationParts[2].split("=")[1]);
                    double z = Double.parseDouble(locationParts[3].split("=")[1]);

                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    String entityIds = resultSet.getString("entityIds");
                    String type = resultSet.getString("type");
                    String owner = resultSet.getString("owner");
                    String particle = resultSet.getString("particle");
                    String visibility = resultSet.getString("visibility");
                    Material icon = Material.matchMaterial(resultSet.getString("icon"));
                    String[] entityIdStrings = entityIds.split(",");
                    Integer[] entityIdsArray = new Integer[entityIdStrings.length];
                    for (int i = 0; i < entityIdStrings.length; i++) {
                        entityIdsArray[i] = Integer.parseInt(entityIdStrings[i]);
                    }
                    String teleportDirection = parseTeleportDirection(resultSet, location);
                    Waystone waystone = new Waystone(uuid, name, location, type, owner, Particles.parse(particle), Visibility.fromString(visibility), Arrays.asList(entityIdsArray), icon, teleportDirection);
                    waystones.add(waystone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystones;
    }

    public static void insertOrUpdateExploredWaystone(String name, String playerId, String waystoneId) {
        insertExploredWaystone(name, playerId, waystoneId);
    }

    public static List<String> getExploredAndPrivateWaystoneIds(String playerId, int pageNumber, int pageSize) {
        List<String> waystoneIds = new ArrayList<>();
        String query = "SELECT waystoneId FROM explored_waystones WHERE playerId = ? UNION " +
                "SELECT waystoneId FROM private_waystones WHERE playerId = ? LIMIT ? OFFSET ?";
        int offset = (pageNumber - 1) * pageSize;

        ResultSet resultSet = DatabaseManager.execute(query, playerId, playerId, pageSize, offset);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    String waystoneId = resultSet.getString("waystoneId");
                    waystoneIds.add(waystoneId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystoneIds;
    }

    public static void saveWaystones(List<Waystone> waystones) {
        String query = "INSERT INTO waystones (id, name, location, entityIds, type, owner, particle, visibility, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (Waystone waystone : waystones) {
            String id = waystone.getId();
            String name = waystone.getName();
            Location location = waystone.getLocation();
            String entityIds = waystone.getEntities().stream().map(String::valueOf).collect(Collectors.joining(","));
            String type = waystone.getType();
            String owner = waystone.getOwnerId();
            String particle = (waystone.getParticle() != null) ? waystone.getParticle().name() : "off";
            String visibility = waystone.getVisibility().name();
            Material icon = waystone.getIcon();

            String locationString = "world=" + location.getWorld().getName() + ",x=" + location.getX() + ",y=" + location.getY() + ",z=" + location.getZ();

            DatabaseManager.executeUpdate(query, id, name, locationString, entityIds, type, owner, particle, visibility, icon.name());
        }
    }

    public static List<Waystone> getWaystones(String playerId, Integer pageNumber, Integer pageSize, String waystoneId) {
        return getWaystones(playerId, pageNumber, pageSize, waystoneId, WaystoneListFilter.ALL);
    }

    public static List<Waystone> getWaystones(String playerId, Integer pageNumber, Integer pageSize, String waystoneId, WaystoneListFilter filter) {
        List<Waystone> waystones = new ArrayList<>();
        if (filter == null) {
            filter = WaystoneListFilter.ALL;
        }

        StringBuilder query = new StringBuilder(
                "SELECT w.id, w.name, w.location, w.entityIds, w.type, w.owner, w.visibility, w.particle, w.icon " +
                "FROM waystones w "
        );
        List<Object> params = new ArrayList<>();
        appendFilterClause(query, params, playerId, waystoneId, filter);
        query.append(" ORDER BY w.name COLLATE NOCASE");

        if (pageNumber != null && pageSize != null) {
            int offset = pageNumber * pageSize;
            query.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add(offset);
        }

        ResultSet resultSet = DatabaseManager.execute(query.toString(), params.toArray());

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    Waystone waystone = waystoneFromResultSet(resultSet);
                    if (waystone != null) {
                        waystones.add(waystone);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystones;
    }

    /**
     * Waystones the player is allowed to see in the teleport list (owned, global, or explored public).
     */
    private static final String ACCESSIBLE_CLAUSE =
            "(" +
            "  w.owner = ? " +
            "  OR UPPER(w.visibility) = 'GLOBAL' " +
            "  OR (UPPER(w.visibility) = 'PUBLIC' AND EXISTS (" +
            "    SELECT 1 FROM explored_waystones e WHERE e.waystoneId = w.id AND e.playerId = ?" +
            "  ))" +
            ")";

    private static void appendFilterClause(StringBuilder query, List<Object> params, String playerId, String waystoneId, WaystoneListFilter filter) {
        switch (filter) {
            case GLOBAL:
                query.append("WHERE UPPER(w.visibility) = 'GLOBAL'");
                break;
            case OWNED:
                query.append("WHERE w.owner = ?");
                params.add(playerId);
                break;
            case EXPLORED:
                query.append("WHERE EXISTS (SELECT 1 FROM explored_waystones e WHERE e.waystoneId = w.id AND e.playerId = ?) ");
                query.append("AND ").append(ACCESSIBLE_CLAUSE);
                params.add(playerId);
                params.add(playerId);
                params.add(playerId);
                break;
            case FAVORITES:
                query.append("WHERE EXISTS (SELECT 1 FROM favorite_waystones f WHERE f.waystoneId = w.id AND f.playerId = ?) ");
                query.append("AND ").append(ACCESSIBLE_CLAUSE);
                params.add(playerId);
                params.add(playerId);
                params.add(playerId);
                break;
            case ALL:
            default:
                query.append("WHERE ").append(ACCESSIBLE_CLAUSE);
                params.add(playerId);
                params.add(playerId);
                break;
        }

        if (waystoneId != null) {
            query.append(" AND w.id <> ?");
            params.add(waystoneId);
        }
    }

    public static List<String> getExploredWaystoneIds(String playerId, Integer pageNumber, Integer pageSize) {
        List<String> waystoneIds = new ArrayList<>();
        String query = "SELECT waystoneId FROM explored_waystones WHERE playerId = ?";

        if (pageNumber != null && pageSize != null) {
            int offset = (pageNumber - 1) * pageSize;
            query += " LIMIT " + pageSize + " OFFSET " + offset;
        }

        ResultSet resultSet = DatabaseManager.execute(query, playerId);

        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    String waystoneId = resultSet.getString("waystoneId");
                    waystoneIds.add(waystoneId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return waystoneIds;
    }


    private static void insertExploredWaystone(String name, String playerId, String waystoneId) {
        String query = "INSERT INTO explored_waystones (playerName, playerId, waystoneId) VALUES (?, ?, ?)";
        DatabaseManager.executeUpdate(query, name, playerId, waystoneId);
    }

    public static List<Map<String, String>> getExplorers(String waystoneId) {
        String query = "SELECT playerId, playerName from explored_waystones we where we.waystoneId = ?";
        ResultSet resultSet = DatabaseManager.execute(query, waystoneId);
        List<Map<String, String>> ids = new ArrayList<>();
        try {
            if (resultSet != null && resultSet.next()) {
                ids.add(new HashMap<String, String>() {
                    {
                        put("playerId", resultSet.getString("playerId"));
                        put("playerName", resultSet.getString("playerName"));
                    }
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ids;
    }

    public static Waystone getWaystone(String uuid) {
        String query = "SELECT * FROM waystones WHERE id = ?";
        ResultSet resultSet = DatabaseManager.execute(query, uuid);

        try {
            if (resultSet != null && resultSet.next()) {
                String name = resultSet.getString("name");

                String locationObject = resultSet.getString("location");
                // Parse JSON manually
                String[] locationParts = locationObject.split(",");
                String worldName = locationParts[0].split("=")[1];
                double x = Double.parseDouble(locationParts[1].split("=")[1]);
                double y = Double.parseDouble(locationParts[2].split("=")[1]);
                double z = Double.parseDouble(locationParts[3].split("=")[1]);

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                String entityIds = resultSet.getString("entityIds");
                String type = resultSet.getString("type");
                String owner = resultSet.getString("owner");
                String particle = resultSet.getString("particle");
                String visibility = resultSet.getString("visibility");

                String[] entityIdStrings = entityIds.split(",");
                Integer[] entityIdsArray = new Integer[entityIdStrings.length];
                for (int i = 0; i < entityIdStrings.length; i++) {
                    entityIdsArray[i] = Integer.parseInt(entityIdStrings[i]);
                }
                Material icon = Material.matchMaterial(resultSet.getString("icon"));
                String teleportDirection = parseTeleportDirection(resultSet, location);
                
                Waystone waystone = new Waystone(uuid, name, location, type, owner, Particles.parse(particle), Visibility.fromString(visibility), Arrays.asList(entityIdsArray), icon, teleportDirection);
                return waystone;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static void insertWaystone(Waystone waystone) {
        String teleportDirection = waystone.getTeleportDirection() != null ? waystone.getTeleportDirection() : "N";
        String query = "INSERT INTO waystones (id, name, location, entityIds, type, owner, icon, visibility, particle, teleportDirection) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DatabaseManager.executeUpdate(
                query,
                waystone.getId(),
                waystone.getName(),
                getLocationString(waystone.getLocation()),
                getEntityIdsString(waystone.getEntities()),
                waystone.getType(),
                waystone.getOwnerId(),
                waystone.getIcon().name(),
                waystone.getVisibility().name(),
                waystone.getParticle() != null ? waystone.getParticle().name() : "off",
                teleportDirection
        );
    }

    private static String getLocationString(Location location) {
        return "world=" + location.getWorld().getName() +
                ",x=" + location.getX() +
                ",y=" + location.getY() +
                ",z=" + location.getZ();
    }

    private static String getTeleportLocationString(Location location) {
        // Include yaw and pitch for teleport locations
        return "world=" + location.getWorld().getName() +
                ",x=" + location.getX() +
                ",y=" + location.getY() +
                ",z=" + location.getZ() +
                ",yaw=" + location.getYaw() +
                ",pitch=" + location.getPitch();
    }

    private static String getEntityIdsString(List<Integer> entityIds) {
        return entityIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static Location parseTeleportLocation(ResultSet resultSet, Location waystoneLocation) throws SQLException {
        try {
            String teleportLocationString = resultSet.getString("teleportLocation");
            if (teleportLocationString != null && !teleportLocationString.isEmpty()) {
                String[] teleportParts = teleportLocationString.split(",");
                if (teleportParts.length >= 4) {
                    String teleportWorldName = teleportParts[0].split("=")[1];
                    double teleportX = Double.parseDouble(teleportParts[1].split("=")[1]);
                    double teleportY = Double.parseDouble(teleportParts[2].split("=")[1]);
                    double teleportZ = Double.parseDouble(teleportParts[3].split("=")[1]);
                    
                    // Parse yaw and pitch if available (new format), otherwise use defaults
                    float teleportYaw = 0.0f;
                    float teleportPitch = 0.0f;
                    if (teleportParts.length >= 6) {
                        teleportYaw = Float.parseFloat(teleportParts[4].split("=")[1]);
                        teleportPitch = Float.parseFloat(teleportParts[5].split("=")[1]);
                    }
                    
                    return new Location(Bukkit.getWorld(teleportWorldName), teleportX, teleportY, teleportZ, teleportYaw, teleportPitch);
                }
            }
        } catch (SQLException e) {
            // Column might not exist, return null
        } catch (Exception e) {
            // Parsing error, return null
        }
        return null;
    }
    

    /**
     * Parses teleport direction from database, with backwards compatibility for old format
     * @param resultSet The database result set
     * @param waystoneLocation The waystone's location (for calculating direction from old format)
     * @return Cardinal direction: "N", "E", "S", or "W"
     */
    private static String parseTeleportDirection(ResultSet resultSet, Location waystoneLocation) {
        try {
            // Try new format first (teleportDirection column)
            String direction = resultSet.getString("teleportDirection");
            if (direction != null && !direction.isEmpty() && (direction.equals("N") || direction.equals("E") || direction.equals("S") || direction.equals("W"))) {
                return direction;
            }
        } catch (SQLException e) {
            // Column doesn't exist, try old format
        }
        
        // Old format: convert from teleportLocation to cardinal direction
        try {
            Location teleportLocation = parseTeleportLocation(resultSet, waystoneLocation);
            if (teleportLocation != null) {
                // Calculate direction FROM waystone TO teleport location
                double waystoneCenterX = waystoneLocation.getX() + 0.5;
                double waystoneCenterZ = waystoneLocation.getZ() + 0.5;
                return positionToCardinalDirection(teleportLocation.getX(), teleportLocation.getZ(), waystoneCenterX, waystoneCenterZ);
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
        // Default to North
        return "N";
    }
    
    /**
     * Converts a yaw angle to the nearest cardinal direction (N, E, S, W)
     * @param yaw The yaw angle in degrees
     * @return "N", "E", "S", or "W"
     */
    public static String yawToCardinalDirection(float yaw) {
        // Normalize yaw to 0-360
        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;
        
        // Minecraft yaw: 0 = South, 90 = West, 180 = North, 270 = East
        // Convert to standard: 0 = North, 90 = East, 180 = South, 270 = West
        float normalizedYaw = (yaw + 180) % 360;
        
        if (normalizedYaw >= 315 || normalizedYaw < 45) {
            return "N"; // North
        } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
            return "E"; // East
        } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
            return "S"; // South
        } else {
            return "W"; // West
        }
    }
    
    /**
     * Determines the closest cardinal direction from a position to a waystone
     * @param fromX X coordinate of the position
     * @param fromZ Z coordinate of the position
     * @param waystoneX X coordinate of waystone center
     * @param waystoneZ Z coordinate of waystone center
     * @return "N", "E", "S", or "W" - the direction FROM waystone TO position
     */
    public static String positionToCardinalDirection(double fromX, double fromZ, double waystoneX, double waystoneZ) {
        double dx = fromX - waystoneX;
        double dz = fromZ - waystoneZ;
        
        // Determine which axis has the larger difference
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "E" : "W";
        } else {
            return dz > 0 ? "S" : "N";
        }
    }
    
    /**
     * Calculates the teleport location based on waystone location and cardinal direction
     * Player is placed 2 blocks away from waystone center in the specified direction, facing the waystone
     * @param waystoneLocation The waystone's base location
     * @param direction Cardinal direction: "N", "E", "S", or "W"
     * @return Location where player should teleport to
     */
    public static Location calculateTeleportLocation(Location waystoneLocation, String direction) {
        double waystoneCenterX = waystoneLocation.getX() + 0.5;
        double waystoneCenterZ = waystoneLocation.getZ() + 0.5;
        double teleportY = waystoneLocation.getY() + 1.2; // 1.2 blocks up from waystone base
        
        double teleportX, teleportZ;
        
        switch (direction.toUpperCase()) {
            case "N":
                // North: -Z direction (north of waystone)
                teleportX = waystoneCenterX;
                teleportZ = waystoneCenterZ - 2.0;
                break;
            case "E":
                // East: +X direction (east of waystone)
                teleportX = waystoneCenterX + 2.0;
                teleportZ = waystoneCenterZ;
                break;
            case "S":
                // South: +Z direction (south of waystone)
                teleportX = waystoneCenterX;
                teleportZ = waystoneCenterZ + 2.0;
                break;
            case "W":
                // West: -X direction (west of waystone)
                teleportX = waystoneCenterX - 2.0;
                teleportZ = waystoneCenterZ;
                break;
            default:
                // Default to north
                teleportX = waystoneCenterX;
                teleportZ = waystoneCenterZ - 2.0;
        }
        
        // Calculate yaw to face the waystone center from teleport location
        double dx = waystoneCenterX - teleportX;
        double dz = waystoneCenterZ - teleportZ;
        // Minecraft yaw: 0 = South, 90 = West, 180 = North, 270 = East
        // atan2(-dx, dz) gives us the angle from teleport location to waystone
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        
        // Set pitch to 0 to look straight ahead (horizontal)
        float pitch = 0.0f;
        
        return new Location(waystoneLocation.getWorld(), teleportX, teleportY, teleportZ, yaw, pitch);
    }


    public static void updateWaystone(Waystone waystone) {
        String teleportDirection = waystone.getTeleportDirection() != null ? waystone.getTeleportDirection() : "N";
        String query = "UPDATE waystones SET name = ?, type = ?, icon = ?, visibility = ?, particle = ?, teleportDirection = ? WHERE id = ?";
        DatabaseManager.executeUpdate(
                query,
                waystone.getName(),
                waystone.getType(),
                waystone.getIcon().name(),
                waystone.getVisibility().name(),
                waystone.getParticle() != null ? waystone.getParticle().name() : "off",
                teleportDirection,
                waystone.getId()
        );
    }

    public static void deleteWaystone(Waystone waystone) {
        waystone.setParticle(null);
        WaystoneMemory.changeParticles(waystone);
        DatabaseManager.removeWaystone(waystone.getId());
    }

    public static int getWaystonesSize(String playerId, String waystoneId) throws SQLException {
        return getWaystonesSize(playerId, waystoneId, WaystoneListFilter.ALL);
    }

    public static int getWaystonesSize(String playerId, String waystoneId, WaystoneListFilter filter) throws SQLException {
        if (filter == null) {
            filter = WaystoneListFilter.ALL;
        }

        StringBuilder query = new StringBuilder("SELECT COUNT(*) as count FROM waystones w ");
        List<Object> params = new ArrayList<>();
        appendFilterClause(query, params, playerId, waystoneId, filter);

        ResultSet resultSet = DatabaseManager.execute(query.toString(), params.toArray());
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static boolean isFavorite(String playerId, String waystoneId) {
        String query = "SELECT 1 FROM favorite_waystones WHERE playerId = ? AND waystoneId = ? LIMIT 1";
        ResultSet resultSet = DatabaseManager.execute(query, playerId, waystoneId);
        try {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if the waystone is favorited after the toggle
     */
    public static boolean toggleFavorite(String playerId, String waystoneId) {
        if (isFavorite(playerId, waystoneId)) {
            DatabaseManager.executeUpdate("DELETE FROM favorite_waystones WHERE playerId = ? AND waystoneId = ?", playerId, waystoneId);
            return false;
        }
        DatabaseManager.executeUpdate("INSERT OR IGNORE INTO favorite_waystones (playerId, waystoneId) VALUES (?, ?)", playerId, waystoneId);
        return true;
    }

    public static WaystoneListFilter getDefaultTeleportFilter(String playerId) {
        String query = "SELECT defaultTeleportFilter FROM player_preferences WHERE playerId = ?";
        ResultSet resultSet = DatabaseManager.execute(query, playerId);
        try {
            if (resultSet != null && resultSet.next()) {
                return WaystoneListFilter.fromString(resultSet.getString("defaultTeleportFilter"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return WaystoneListFilter.ALL;
    }

    public static void setDefaultTeleportFilter(String playerId, WaystoneListFilter filter) {
        if (filter == null) {
            filter = WaystoneListFilter.ALL;
        }
        DatabaseManager.executeUpdate(
                "INSERT INTO player_preferences (playerId, defaultTeleportFilter) VALUES (?, ?) " +
                        "ON CONFLICT(playerId) DO UPDATE SET defaultTeleportFilter = excluded.defaultTeleportFilter",
                playerId,
                filter.name()
        );
    }

    private static Waystone waystoneFromResultSet(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String name = resultSet.getString("name");
        String locationObject = resultSet.getString("location");
        if (locationObject == null) {
            return null;
        }

        String[] locationParts = locationObject.split(",");
        if (locationParts.length != 4) {
            return null;
        }

        String worldName = locationParts[0].split("=")[1];
        double x = Double.parseDouble(locationParts[1].split("=")[1]);
        double y = Double.parseDouble(locationParts[2].split("=")[1]);
        double z = Double.parseDouble(locationParts[3].split("=")[1]);
        Location location = new Location(Bukkit.getWorld(worldName), x, y, z);

        String type = resultSet.getString("type");
        String owner = resultSet.getString("owner");
        String particle = resultSet.getString("particle");
        String visibility = resultSet.getString("visibility");
        Material icon = Material.matchMaterial(resultSet.getString("icon"));
        if (icon == null) {
            icon = Material.LODESTONE;
        }

        List<Integer> entityIds = parseEntityIds(resultSet.getString("entityIds"));
        String teleportDirection = parseTeleportDirection(resultSet, location);
        return new Waystone(id, name, location, type, owner, Particles.parse(particle),
                Visibility.fromString(visibility), entityIds, icon, teleportDirection);
    }

    private static List<Integer> parseEntityIds(String entityIds) {
        if (entityIds == null || entityIds.isBlank()) {
            return Collections.emptyList();
        }
        List<Integer> ids = new ArrayList<>();
        for (String part : entityIds.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                // Skip malformed entries from older data
            }
        }
        return ids;
    }
}
