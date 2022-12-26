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
package io.github.quantizr.dungeonrooms.utils

import io.github.quantizr.dungeonrooms.ChatTransmitter
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.handlers.ScoreboardHandler.cleanSB
import io.github.quantizr.dungeonrooms.handlers.ScoreboardHandler.sidebarLines
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.ChatComponentText
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.BaseConfiguration
import org.apache.logging.log4j.core.config.LoggerConfig
import java.io.File
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.InflaterInputStream

object Utils {
    @JvmField
    var inSkyblock = false

    @JvmField
    var inCatacombs = false
    var dungeonOverride = false

    /**
     * Taken from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    @JvmStatic
    fun checkForSkyblock() {
        if (dungeonOverride) {
            inSkyblock = true
            return
        }
        val mc = Minecraft.getMinecraft()
        if (mc?.theWorld != null && !mc.isSingleplayer) {
            val scoreboardObj = mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
            if (scoreboardObj != null) {
                val scObjName = cleanSB(scoreboardObj.displayName)
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true
                    return
                }
            }
        }
        inSkyblock = false
    }

    /**
     * Taken from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    @JvmStatic
    fun checkForCatacombs() {
        if (dungeonOverride) {
            inCatacombs = true
            return
        }
        if (inSkyblock) {
            val scoreboard = sidebarLines
            for (s in scoreboard) {
                val sCleaned = cleanSB(s)
                if (sCleaned.contains("The Catacombs")) {
                    inCatacombs = true
                    return
                }
            }
        }
        inCatacombs = false
    }

    /**
     * @return List of the paths to every .skeleton room data file
     */
    @JvmStatic
    fun getAllPaths(folderName: String): List<Path> {
        return ArrayList<Path>().also { paths ->
            Files.walk(Paths.get(DungeonRooms::class.java.getResource("/assets/dungeonrooms/$folderName")!!.path), 3)
                .filter { p: Path -> p.toString().endsWith(".skeleton") }
                .forEach { p: Path -> paths.add(p) }
        }
    }

    /**
     * Converts the .skeleton files into a readable format.
     * @return room data as a hashmap
     */
    fun pathsToRoomData(parentFolder: String, allPaths: List<Path>): HashMap<String, LongArray> {
        val allRoomData = HashMap<String, LongArray>()

        for (path in allPaths) {
            if (path.parent.fileName.toString() == parentFolder) {
                val name = path.fileName.toString()
                val file = "/assets" + path.toString().split("assets")[1]
                val input = DungeonRooms::instance::class.java.getResourceAsStream(file)
                val data = ObjectInputStream(InflaterInputStream(input))
                val roomData = data.readObject() as LongArray
                allRoomData[name.replace(".skeleton", "")] = roomData
                DungeonRooms.logger.debug("DungeonRooms: Loaded $name")
            }
        }
        DungeonRooms.logger.info("DungeonRooms: Loaded ${allRoomData.size} $parentFolder rooms")
        return allRoomData
    }

    /**
     * Used to set the log level of just this mod
     */
    fun setLogLevel(logger: Logger, level: Level?) {
        val ctx = LogManager.getContext(false) as LoggerContext
        val config = ctx.configuration as BaseConfiguration
        val loggerConfig = config.getLoggerConfig(logger.name)
        var specificConfig = loggerConfig
        if (loggerConfig.name != logger.name) {
            specificConfig = LoggerConfig(logger.name, level, true)
            specificConfig.parent = loggerConfig
            config.addLogger(logger.name, specificConfig)
        }
        specificConfig.level = level
        ctx.updateLoggers()
    }

    /**
     * Packs block info into a single 8 byte primitive long. Normally, first a pair of bytes will be x coordinate, second
     * pair will be y coordinate, third pair will be z coordinate, and last pair will be block id and metadata.
     * @return primitive long containing block info
     */
    fun shortToLong(a: Short, b: Short, c: Short, d: Short): Long {
        return (a.toInt() shl 16 or (b.toInt() and 0xFFFF)).toLong() shl 32 or ((c.toInt() shl 16 or (d.toInt() and 0xFFFF)).toLong() and 0xFFFFFFFFL)
    }

    /**
     * @return Array of four shorts containing the values stored in the long
     */
    fun longToShort(l: Long): ShortArray {
        return shortArrayOf((l shr 48).toShort(), (l shr 32).toShort(), (l shr 16).toShort(), l.toShort())
    }
}