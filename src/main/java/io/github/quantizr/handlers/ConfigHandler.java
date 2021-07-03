/*
Taken from Danker's Skyblock Mod (https://github.com/bowser0000/SkyblockMod/).
This file was released under GNU General Public License v3.0 and remains under said license.
Modified by Quantizr (_risk) in Feb. 2021.
*/

package io.github.quantizr.handlers;

import io.github.quantizr.DungeonRooms;
import io.github.quantizr.core.AutoRoom;
import io.github.quantizr.core.Waypoints;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
    public static Configuration config;
    private final static String file = "config/DungeonRooms.cfg";

    public static void init() {
        config = new Configuration(new File(file));
        try {
            config.load();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static int getInt(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, 0).getInt();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return 0;
    }

    public static double getDouble(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, 0D).getDouble();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return 0D;
    }

    public static String getString(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, "").getString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return "";
    }

    public static boolean getBoolean(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, false).getBoolean();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return true;
    }

    public static void writeIntConfig(String category, String key, int value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            int set = config.get(category, key, value).getInt();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static void writeDoubleConfig(String category, String key, double value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            double set = config.get(category, key, value).getDouble();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static void writeStringConfig(String category, String key, String value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            String set = config.get(category, key, value).getString();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static void writeBooleanConfig(String category, String key, boolean value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            boolean set = config.get(category, key, value).getBoolean();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static boolean hasKey(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (!config.hasCategory(category)) return false;
            return config.getCategory(category).containsKey(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return false;
    }

    public static void deleteCategory(String category) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.hasCategory(category)) {
                config.removeCategory(new ConfigCategory(category));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static void reloadConfig() {
        if (!hasKey("toggles", "chatToggled")) writeBooleanConfig("toggles", "chatToggled", false);
        if (!hasKey("toggles", "guiToggled")) writeBooleanConfig("toggles", "guiToggled", true);
        if (!hasKey("toggles", "coordToggled")) writeBooleanConfig("toggles", "coordToggled", false);
        if (!hasKey("toggles", "waypointsToggled")) writeBooleanConfig("toggles", "waypointsToggled", true);

        if (!hasKey("waypoint", "showEntrance")) writeBooleanConfig("waypoint", "showEntrance", true);
        if (!hasKey("waypoint", "showSuperboom")) writeBooleanConfig("waypoint", "showSuperboom", true);
        if (!hasKey("waypoint", "showSecrets")) writeBooleanConfig("waypoint", "showSecrets", true);
        if (!hasKey("waypoint", "showFairySouls")) writeBooleanConfig("waypoint", "showFairySouls", true);
        if (!hasKey("waypoint", "sneakToDisable")) writeBooleanConfig("waypoint", "sneakToDisable", true);
        if (!hasKey("waypoint", "disableWhenAllFound")) writeBooleanConfig("waypoint", "disableWhenAllFound", true);

        if (!hasKey("waypoint", "showWaypointText")) writeBooleanConfig("waypoint", "showWaypointText", true);
        if (!hasKey("waypoint", "showBoundingBox")) writeBooleanConfig("waypoint", "showBoundingBox", true);
        if (!hasKey("waypoint", "showBeacon")) writeBooleanConfig("waypoint", "showBeacon", true);

        if (!hasKey("gui", "scaleX")) writeIntConfig("gui", "scaleX", 50);
        if (!hasKey("gui", "scaleY")) writeIntConfig("gui", "scaleY", 5);
        if (!hasKey("gui", "hotkeyOpen")) writeStringConfig("gui", "hotkeyOpen", "gui");

        AutoRoom.chatToggled = getBoolean("toggles", "chatToggled");
        AutoRoom.guiToggled = getBoolean("toggles", "guiToggled");
        AutoRoom.coordToggled = getBoolean("toggles", "coordToggled");
        Waypoints.enabled =  getBoolean("toggles", "waypointsToggled");

        Waypoints.showEntrance = getBoolean("waypoint", "showEntrance");
        Waypoints.showSuperboom = getBoolean("waypoint", "showSuperboom");
        Waypoints.showSecrets = getBoolean("waypoint", "showSecrets");
        Waypoints.showFairySouls = getBoolean("waypoint", "showFairySouls");
        Waypoints.sneakToDisable = getBoolean("waypoint", "sneakToDisable");
        Waypoints.disableWhenAllFound = getBoolean("waypoint", "disableWhenAllFound");

        Waypoints.showWaypointText = getBoolean("waypoint", "showWaypointText");
        Waypoints.showBoundingBox = getBoolean("waypoint", "showBoundingBox");
        Waypoints.showBeacon = getBoolean("waypoint", "showBeacon");

        AutoRoom.scaleX = getInt("gui", "scaleX");
        AutoRoom.scaleY = getInt("gui", "scaleY");
        DungeonRooms.hotkeyOpen = getString("gui", "hotkeyOpen");
    }
}
