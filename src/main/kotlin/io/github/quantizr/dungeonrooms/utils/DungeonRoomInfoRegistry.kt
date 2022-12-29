package io.github.quantizr.dungeonrooms.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.quantizr.dungeonrooms.DungeonRooms
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.util.*

object DungeonRoomInfoRegistry {
    val logger: Logger = LogManager.getLogger("DungeonRoomInfoRegistry")

    val thing: MutableSet<JsonObject> = HashSet()


    @JvmStatic
    fun loadAll(dir: File) {
        val gson = Gson()

        val lines = IOUtils.readLines(
            Objects.requireNonNull(
                DungeonRooms::class.java.getResourceAsStream("/roomdataindex.txt")
            )
        )
        for (name in lines) {
            if (name.endsWith(".json")) {
                try {
                    DungeonRooms::class.java.getResourceAsStream("/$name")
                        .use { fis ->
                            fis?.let {
                                InputStreamReader(fis).use { yas ->
                                    thing.add(gson.fromJson(yas, JsonObject::class.java))
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.error(name)
                    e.printStackTrace()
                }
            }
        }
    }
}