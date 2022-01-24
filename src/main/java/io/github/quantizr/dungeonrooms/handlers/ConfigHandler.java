/*
 * Dungeon Rooms Mod - Secret Waypoints for Hypixel Skyblock Dungeons
 * Copyright 2021 Quantizr(_risk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.quantizr.dungeonrooms.handlers;

import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.DungeonManager;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.Waypoints;
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
        if (!hasKey("toggles", "guiToggled")) writeBooleanConfig("toggles", "guiToggled", true);
        if (!hasKey("toggles", "motdToggled")) writeBooleanConfig("toggles", "motdToggled", true);
        if (!hasKey("toggles", "waypointsToggled")) writeBooleanConfig("toggles", "waypointsToggled", true);

        if (!hasKey("waypoint", "showEntrance")) writeBooleanConfig("waypoint", "showEntrance", true);
        if (!hasKey("waypoint", "showSuperboom")) writeBooleanConfig("waypoint", "showSuperboom", true);
        if (!hasKey("waypoint", "showSecrets")) writeBooleanConfig("waypoint", "showSecrets", true);
        if (!hasKey("waypoint", "showFairySouls")) writeBooleanConfig("waypoint", "showFairySouls", true);
        if (!hasKey("waypoint", "showStonk")) writeBooleanConfig("waypoint", "showStonk", true);
        if (!hasKey("waypoint", "sneakToDisable")) writeBooleanConfig("waypoint", "sneakToDisable", true);
        if (!hasKey("waypoint", "disableWhenAllFound")) writeBooleanConfig("waypoint", "disableWhenAllFound", true);

        if (!hasKey("waypoint", "showWaypointText")) writeBooleanConfig("waypoint", "showWaypointText", true);
        if (!hasKey("waypoint", "showBoundingBox")) writeBooleanConfig("waypoint", "showBoundingBox", true);
        if (!hasKey("waypoint", "showBeacon")) writeBooleanConfig("waypoint", "showBeacon", true);

        if (!hasKey("waypoint", "practiceModeOn")) writeBooleanConfig("waypoint", "practiceModeOn", false);

        if (!hasKey("gui", "scaleX")) writeIntConfig("gui", "scaleX", 50);
        if (!hasKey("gui", "scaleY")) writeIntConfig("gui", "scaleY", 5);
        if (!hasKey("gui", "hotkeyOpen")) writeStringConfig("gui", "hotkeyOpen", "gui");

        //run special messages on first login
        if (!hasKey("drm", "version")) {
            writeStringConfig("drm", "version", DungeonRooms.VERSION);
            DungeonRooms.firstLogin = true;
            //writeBooleanConfig("toggles", "waypointsToggled", false);
        } else if (!getString("drm", "version").equals(DungeonRooms.VERSION)) {
            writeStringConfig("drm", "version", DungeonRooms.VERSION);
            /* //uncomment if major update in future requires firstLogin prompt to be displayed again
            DungeonRooms.firstLogin = true;
            writeBooleanConfig("toggles", "waypointsToggled", false);
            */
        }

        //AutoRoom.chatToggled = getBoolean("toggles", "chatToggled");
        DungeonManager.guiToggled = getBoolean("toggles", "guiToggled");
        DungeonManager.motdToggled = getBoolean("toggles", "motdToggled");
        //AutoRoom.coordToggled = getBoolean("toggles", "coordToggled");
        Waypoints.enabled =  getBoolean("toggles", "waypointsToggled");

        Waypoints.showEntrance = getBoolean("waypoint", "showEntrance");
        Waypoints.showSuperboom = getBoolean("waypoint", "showSuperboom");
        Waypoints.showSecrets = getBoolean("waypoint", "showSecrets");
        Waypoints.showFairySouls = getBoolean("waypoint", "showFairySouls");
        Waypoints.showStonk = getBoolean("waypoint", "showStonk");
        Waypoints.sneakToDisable = getBoolean("waypoint", "sneakToDisable");
        Waypoints.disableWhenAllFound = getBoolean("waypoint", "disableWhenAllFound");

        Waypoints.showWaypointText = getBoolean("waypoint", "showWaypointText");
        Waypoints.showBoundingBox = getBoolean("waypoint", "showBoundingBox");
        Waypoints.showBeacon = getBoolean("waypoint", "showBeacon");

        Waypoints.practiceModeOn = getBoolean("waypoint", "practiceModeOn");

        DungeonRooms.textLocX = getInt("gui", "scaleX");
        DungeonRooms.textLocY = getInt("gui", "scaleY");
        DungeonRooms.imageHotkeyOpen = getString("gui", "hotkeyOpen");
    }
}
