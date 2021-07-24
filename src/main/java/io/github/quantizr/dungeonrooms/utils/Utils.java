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

package io.github.quantizr.dungeonrooms.utils;

import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.handlers.ScoreboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.BaseConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

public class Utils {
    public static boolean inSkyblock = false;
    public static boolean inCatacombs = false;
    public static boolean dungeonOverride = false;

    /**
     * Taken from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static void checkForSkyblock() {
        if (dungeonOverride) {
            inSkyblock = true;
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null && !mc.isSingleplayer()) {
            ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (scoreboardObj != null) {
                String scObjName = ScoreboardHandler.cleanSB(scoreboardObj.getDisplayName());
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true;
                    return;
                }
            }
        }
        inSkyblock = false;
    }

    /**
     * Taken from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static void checkForCatacombs() {
        if (dungeonOverride) {
            inCatacombs = true;
            return;
        }
        if (inSkyblock) {
            List<String> scoreboard = ScoreboardHandler.getSidebarLines();
            for (String s : scoreboard) {
                String sCleaned = ScoreboardHandler.cleanSB(s);
                if (sCleaned.contains("The Catacombs")) {
                    inCatacombs = true;
                    return;
                }
            }
        }
        inCatacombs = false;
    }


    public static void checkForConflictingHotkeys() {
        Minecraft mc = Minecraft.getMinecraft();
        for (KeyBinding drmKeybind : DungeonRooms.keyBindings) {
            for (KeyBinding keybinding : mc.gameSettings.keyBindings) {
                if (drmKeybind.getKeyCode() != 0 && drmKeybind != keybinding && drmKeybind.getKeyCode() == keybinding.getKeyCode()) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§d§l--- Dungeon Rooms Mod ---\n"
                            + " §r§cThe hotkey \"" + GameSettings.getKeyDisplayString(drmKeybind.getKeyCode())
                            + "\", which is used to " + drmKeybind.getKeyDescription() + ", has a conflict with a "
                            + "keybinding from \"" + keybinding.getKeyCategory() + "\".\n §c§lPlease go into the "
                            + "Minecraft Controls menu and change one of the keybindings.\n"
                            + "§d§l------------------------"
                    ));
                }
            }
        }

    }

    /**
     * @return List of the paths to every .skeleton room data file
     */
    public static List<Path> getAllPaths (String folderName) {
        List<Path> paths = new ArrayList<>();
        try {
            URI uri = DungeonRooms.class.getResource("/assets/dungeonrooms/" + folderName).toURI();
            Path Path;
            FileSystem fileSystem = null;
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                Path = fileSystem.getPath("/assets/dungeonrooms/" + folderName);
            } else {
                Path = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(Path, 3);
            for (Iterator<Path> it = walk.iterator(); it.hasNext();) {
                Path path = it.next();
                String name = path.getFileName().toString();
                if (name.endsWith(".skeleton")) paths.add(path);
            }
            if (fileSystem != null) fileSystem.close();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * Converts the .skeleton files into a readable format.
     * @return room data as a hashmap
     */
    public static HashMap<String, long[]> pathsToRoomData (String parentFolder, List<Path> allPaths) {
        HashMap<String, long[]> allRoomData = new HashMap<>();
        try {
            for (Path path : allPaths) {
                if (!path.getParent().getFileName().toString().equals(parentFolder)) continue;
                String name = path.getFileName().toString();
                InputStream input = DungeonRooms.class.getResourceAsStream(path.toString());
                ObjectInputStream data = new ObjectInputStream(new InflaterInputStream(input));
                long[] roomData = (long[]) data.readObject();
                allRoomData.put(name.substring(0, name.length() - 9), roomData);
                DungeonRooms.logger.debug("DungeonRooms: Loaded " + name);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        DungeonRooms.logger.info("DungeonRooms: Loaded " + allRoomData.size() + " " + parentFolder + " rooms");
        return allRoomData;
    }

    /**
     * Used to set the log level of just this mod
     */
    public static void setLogLevel(Logger logger, Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        BaseConfiguration config = (BaseConfiguration) ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        LoggerConfig specificConfig = loggerConfig;

        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }

    /**
     * Packs block info into a single 8 byte primitive long. Normally, first pair of bytes will be x coordinate, second
     * pair will be y coordinate, third pair will be z coordinate, and last pair will be block id and metadata.
     * @return primitive long containing block info
     */
    public static long shortToLong(short a, short b, short c, short d) {
        return ((long)((a << 16) | (b & 0xFFFF)) << 32) | (((c << 16) | (d & 0xFFFF)) & 0xFFFFFFFFL);
    }

    /**
     * @return Array of four shorts containing the values stored in the long
     */
    public static short[] longToShort(long l) {
        return new short[]{(short) (l >> 48), (short) (l >> 32), (short) (l >> 16), (short) (l)};
    }
}
