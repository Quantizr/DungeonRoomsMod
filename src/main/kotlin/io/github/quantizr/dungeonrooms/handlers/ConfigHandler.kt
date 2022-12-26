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
package io.github.quantizr.dungeonrooms.handlers

import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.dungeons.DungeonManager
import io.github.quantizr.dungeonrooms.dungeons.Waypoints
import net.minecraftforge.common.config.ConfigCategory
import net.minecraftforge.common.config.Configuration
import java.io.File

object ConfigHandler {
    private var config: Configuration? = null
    private const val file = "config/DungeonRooms.cfg"

    private fun getString(category: String?, key: String?): String {
        config = Configuration(File(file))
        try {
            config!!.load()
            if (config!!.getCategory(category).containsKey(key)) {
                return config!![category, key, ""].string
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            config!!.save()
        }
        return ""
    }

    private fun writeStringConfig(category: String?, key: String?, value: String?) {
        config = Configuration(File(file))
        try {
            config!!.load()
            val set = config!![category, key, value].string
            config!!.getCategory(category)[key].set(value)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            config!!.save()
        }
    }
    private fun hasKey(category: String?, key: String?): Boolean {
        config = Configuration(File(file))
        try {
            config!!.load()
            return if (!config!!.hasCategory(category)) false else config!!.getCategory(category).containsKey(key)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            config!!.save()
        }
        return false
    }


    @JvmStatic
    fun reloadConfig() {
        if (!hasKey("drm", "version")) {
            writeStringConfig("drm", "version", DungeonRooms.VERSION)
            DungeonRooms.firstLogin = true
        } else if (getString("drm", "version") != DungeonRooms.VERSION) {
            writeStringConfig("drm", "version", DungeonRooms.VERSION)
        }

    }
}