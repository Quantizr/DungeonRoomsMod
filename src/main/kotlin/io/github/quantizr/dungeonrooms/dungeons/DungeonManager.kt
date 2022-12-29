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
package io.github.quantizr.dungeonrooms.dungeons

import io.github.quantizr.dungeonrooms.ChatTransmitter
import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.DungeonRooms.Companion.instance
import io.github.quantizr.dungeonrooms.pathfinding.CachedPathFinder
import io.github.quantizr.dungeonrooms.utils.MapUtils
import io.github.quantizr.dungeonrooms.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Point
import java.util.*

class DungeonManager {
    val mc: Minecraft = Minecraft.getMinecraft()
    private var bloodTime = Long.MAX_VALUE
    private var oddRun = true //if current run number is even or odd
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inCatacombs) return
        val message = event.message.formattedText

        //gameStage set from 0 to 1 in the onTick function later
        if (message.startsWith("§e[NPC] §bMort§f: §rHere, I found this map when I first entered the dungeon.§r")) {
            gameStage = DungeonRunStage.RoomClear
            DungeonRooms.logger.info("DungeonRooms: gameStage set to $gameStage")
        } else if (message.startsWith("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodTime = System.currentTimeMillis() + 5000 //5 seconds because additional messages might come through
            DungeonRooms.logger.info("DungeonRooms: bloodDone has been set to True")
        } else if (System.currentTimeMillis() > bloodTime && (message.startsWith("§r§c[BOSS] ") && !message.contains(" The Watcher§r§f:") || message.startsWith(
                "§r§4[BOSS] "
            ))
        ) {
            if (gameStage != DungeonRunStage.RoomClear) {
                gameStage = DungeonRunStage.RoomClear
                DungeonRooms.logger.info("DungeonRooms: gameStage set to $gameStage")

                //this part mostly so /room json doesn't error out
                instance.roomDetection.resetCurrentRoom()
                instance.roomDetection.roomName = "Boss Room"
                instance.roomDetection.roomCategory = "General"
                //RoomDetection.newRoom() //uncomment to display Boss Room in gui
            }
        } else if (message.contains("§r§c☠ §r§eDefeated §r")) {
            gameStage = DungeonRunStage.Boss
            DungeonRooms.logger.info("DungeonRooms: gameStage set to $gameStage")
            instance.roomDetection.resetCurrentRoom()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        val player = mc.thePlayer
        if (!Utils.inCatacombs) return  //From this point forward, everything assumes that Utils.inCatacombs == true
        tickAmount++
        if ((gameStage == DungeonRunStage.NotInDungeon || gameStage == DungeonRunStage.NotStarted) && tickAmount % 20 == 0) {
            if (DungeonRooms.firstLogin) {
                DungeonRooms.firstLogin = false
                ChatTransmitter.addToQueue(
                        """
                        §d§l--- Dungeon Rooms Mod ---
                        §e This appears to be your first time using DRM v${DungeonRooms.VERSION}.
                        §e Press "${DRMConfig.waypointGuiKey.display}" to configure Secret Waypoint settings, If you do not wish to use Waypoints, you can instead press "${DRMConfig.openSecretImages.display}" while inside a dungeon room to view images of the secrets for that room.
                        §r (If you need help, join the Discord! Run "/room discord" to open the Discord invite.)
                        §d§l------------------------
                        """.trimIndent()
                    )

            }
            if (gameStage == DungeonRunStage.NotInDungeon) {
                gameStage = DungeonRunStage.NotStarted
                DungeonRooms.logger.info("DungeonRooms: gameStage set to $gameStage")
            }
            if (MapUtils.updatedMap() != null) {
                DungeonRooms.logger.warn("DungeonRooms: Run started but gameStage is not on 2")
                gameStage = DungeonRunStage.RoomClear
                DungeonRooms.logger.info("DungeonRooms: gameStage set to $gameStage")
                return
            }
            if (gameStage == DungeonRunStage.NotStarted && entrancePhysicalNWCorner == null) {
                if (player.positionVector != Vec3(0.0, 0.0, 0.0)) {
                    //this point is calculated using math, not scanning, which may cause issues when reconnecting to a run
                    entrancePhysicalNWCorner = MapUtils.getClosestNWPhysicalCorner(player.positionVector)
                    DungeonRooms.logger.info("DungeonRooms: entrancePhysicalNWCorner has been set to $entrancePhysicalNWCorner")
                }
            }
            if (DRMConfig.motdToggled) {
                DungeonRooms.logger.info("DungeonRooms: Updating MOTD on screen")
                if (oddRun || !DRMConfig.guiToggled) { // load MOTD on odd runs
                    if (instance.motd.isNotEmpty()) {
                        instance.textToDisplay = instance.motd
                    }
                }
                if (DRMConfig.guiToggled) { //if MOTD is empty or not odd run load default text
                    instance.textToDisplay = ArrayList(
                        listOf(
                            "Dungeon Rooms: ${EnumChatFormatting.GREEN}Press the hotkey \"${
                                DRMConfig.waypointGuiKey.display
                            }\" to configure",
                            "${EnumChatFormatting.GREEN}waypoint settings. Alternatively, press \"${
                                DRMConfig.openSecretImages.display
                            }\" while in a room",
                            "${EnumChatFormatting.GREEN}to view images of secret locations for that room.",
                            "(You can change the keybinds in Minecraft controls menu)"
                        )
                    )
                }
                oddRun = !oddRun
            }
            tickAmount = 0
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload?) {
        Utils.inCatacombs = false
        tickAmount = 0
        gameStage = DungeonRunStage.NotInDungeon
        map = null
        entranceMapCorners = null
        entrancePhysicalNWCorner = null
        instance.roomDetection.entranceMapNullCount = 0
        bloodTime = Long.MAX_VALUE
        instance.roomDetection.stage2Executor.shutdown()
        instance.waypoints.allSecretsMap.clear()
        instance.roomDetection.resetCurrentRoom()
    }

    var gameStage = DungeonRunStage.NotInDungeon

    companion object {
//        @JvmField
//        var gameStage = 0 //0: Not in Dungeon, 1: Entrance/Not Started, 2: Room Clear, 3: Boss, 4: Done

        @JvmField
        var map: Array<Array<Int?>>? = null

        @JvmField
        var entranceMapCorners: Array<Point?>? = null

        @JvmField
        var entrancePhysicalNWCorner: Point? = null
        var tickAmount = 0
    }
}