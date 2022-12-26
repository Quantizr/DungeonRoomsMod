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

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.events.PacketEvent.ReceiveEvent
import io.github.quantizr.dungeonrooms.utils.MapUtils
import io.github.quantizr.dungeonrooms.utils.Utils
import io.github.quantizr.dungeonrooms.utils.WaypointUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S0DPacketCollectItem
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import java.awt.Color

class Waypoints {
    private var frustum = Frustum()

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!DRMConfig.waypointsEnabled) return
        if (DRMConfig.practiceModeOn && !DRMConfig.practiceModeKeyBind.isActive) return

        DungeonRooms.instance.forEverySecretInRoom { (secretsObject, _) ->
            var display = true
            for (j in 1..secretCount) {
                if (!secretsList!![j - 1]) {
                    if (secretsObject["secretName"].asString.substring(0, 2)
                            .replace("[\\D]".toRegex(), "") == j.toString()
                    ) {
                        display = false
                        break
                    }
                }
            }
            if (!display) return@forEverySecretInRoom
            if (DRMConfig.disableWhenAllFound && allFound && secretsObject["category"].asString != "fairysoul") return@forEverySecretInRoom
            val relative = BlockPos(secretsObject["x"].asInt, secretsObject["y"].asInt, secretsObject["z"].asInt)
            val pos = MapUtils.relativeToActual(
                relative,
                DungeonRooms.instance.roomDetection.roomDirection,
                DungeonRooms.instance.roomDetection.roomCorner!!
            )
            val viewer = Minecraft.getMinecraft().renderViewEntity
            frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ)
            if (!frustum.isBoxInFrustum(
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble(),
                    (pos.x + 1).toDouble(),
                    255.0,
                    (pos.z + 1).toDouble()
                )
            ) {
                return@forEverySecretInRoom
            }
            val color = when (secretsObject["category"].asString) {
                "entrance" -> {
                    if (!DRMConfig.showEntrance) return@forEverySecretInRoom
                    Color(0, 255, 0)
                }

                "superboom" -> {
                    if (!DRMConfig.showSuperboom) return@forEverySecretInRoom
                    Color(255, 0, 0)
                }

                "chest" -> {
                    if (!DRMConfig.showSecrets) return@forEverySecretInRoom
                    Color(2, 213, 250)
                }

                "item" -> {
                    if (!DRMConfig.showSecrets) return@forEverySecretInRoom
                    Color(2, 64, 250)
                }

                "bat" -> {
                    if (!DRMConfig.showSecrets) return@forEverySecretInRoom
                    Color(142, 66, 0)
                }

                "wither" -> {
                    if (!DRMConfig.showSecrets) return@forEverySecretInRoom
                    Color(30, 30, 30)
                }

                "lever" -> {
                    if (!DRMConfig.showSecrets) return@forEverySecretInRoom
                    Color(250, 217, 2)
                }

                "fairysoul" -> {
                    if (!DRMConfig.showFairySouls) return@forEverySecretInRoom
                    Color(255, 85, 255)
                }

                "stonk" -> {
                    if (!DRMConfig.showStonk) return@forEverySecretInRoom
                    Color(146, 52, 235)
                }

                else -> Color(190, 255, 252)
            }
            val viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks
            val viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks
            val viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            val distSq = x * x + y * y + z * z
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            if (DRMConfig.showBoundingBox && frustum.isBoxInFrustum(
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble(),
                    (pos.x + 1).toDouble(),
                    (pos.y + 1).toDouble(),
                    (pos.z + 1).toDouble()
                )
            ) {
                WaypointUtils.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, 0.4f)
            }
            GlStateManager.disableTexture2D()
            if (DRMConfig.showBeacon && distSq > 5 * 5) WaypointUtils.renderBeaconBeam(
                x,
                y + 1,
                z,
                color.rgb,
                0.25f,
                event.partialTicks
            )
            if (DRMConfig.showWaypointText) WaypointUtils.renderWaypointText(
                secretsObject["secretName"].asString,
                pos.up(2),
                event.partialTicks
            )
            GlStateManager.disableLighting()
            GlStateManager.enableTexture2D()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled) return
        // Action Bar
        if (event.type.toInt() != 2) return
        event.message.unformattedText.split(" {3,}".toRegex()).forEach { section ->
            if (section.contains("Secrets") && section.contains("/")) {
                val cleanedSection = StringUtils.stripControlCodes(section)
                val splitSecrets = cleanedSection.split("/")
                completedSecrets = splitSecrets[0].replace("[^0-9]".toRegex(), "").toInt()
                val totalSecrets = splitSecrets[1].replace("[^0-9]".toRegex(), "").toInt()
                allFound = totalSecrets == secretCount && completedSecrets == secretCount
                return
            }
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled) return
        if (DRMConfig.disableWhenAllFound && allFound) return
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return
        val block = event.world.getBlockState(event.pos).block
        if (block != Blocks.chest && block != Blocks.skull) return

        DungeonRooms.instance.forEverySecretInRoom { (secretsObject, roomName) ->
            if (secretsObject["category"].asString == "chest" || secretsObject["category"].asString == "wither") {
                val relative =
                    BlockPos(secretsObject["x"].asInt, secretsObject["y"].asInt, secretsObject["z"].asInt)
                val pos =
                    MapUtils.relativeToActual(
                        relative,
                        DungeonRooms.instance.roomDetection.roomDirection,
                        DungeonRooms.instance.roomDetection.roomCorner!!
                    )
                if (pos == event.pos) {
                    for (j in 1..secretCount) {
                        if (secretsObject["secretName"].asString.substring(0, 2)
                                .replace("[\\D]".toRegex(), "") == j.toString()
                        ) {
                            secretsList!![j - 1] = false
                            allSecretsMap.replace(roomName, secretsList)
                            DungeonRooms.logger.info("DungeonRooms: Detected " + secretsObject["category"].asString + " click, turning off waypoint for secret #" + j)
                            break
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled) return
        if (DRMConfig.disableWhenAllFound && allFound) return
        val mc = Minecraft.getMinecraft()
        if (event.packet is S0DPacketCollectItem) {
            val packet = event.packet as S0DPacketCollectItem
            var entity = mc.theWorld.getEntityByID(packet.collectedItemEntityID)
            if (entity is EntityItem) {
                val item = entity
                entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                val name = item.entityItem.displayName
                if (!name.contains("Decoy")
                    && !name.contains("Defuse Kit")
                    && !name.contains("Dungeon Chest Key")
                    && !name.contains("Healing VIII")
                    && !name.contains("Inflatable Jerry")
                    && !name.contains("Spirit Leap")
                    && !name.contains("Training Weights")
                    && !name.contains("Trap")
                    && !name.contains("Treasure Talisman")
                ) {
                    return
                }
                if (entity.commandSenderEntity.name != mc.thePlayer.name) {
                    // Do nothing if someone else picks up the item in order to follow Hypixel rules
                    return
                }

                DungeonRooms.instance.forEverySecretInRoom { (secretsObject, roomName) ->
                    if (secretsObject["category"].asString == "item" || secretsObject["category"].asString == "bat") {
                        val relative = BlockPos(
                            secretsObject["x"].asInt,
                            secretsObject["y"].asInt,
                            secretsObject["z"].asInt
                        )
                        val pos = MapUtils.relativeToActual(
                            relative,
                            DungeonRooms.instance.roomDetection.roomDirection,
                            DungeonRooms.instance.roomDetection.roomCorner!!
                        )
                        if (entity.getDistanceSq(pos) <= 36.0) {
                            for (j in 1..secretCount) {
                                if (secretsObject["secretName"].asString.substring(0, 2)
                                        .replace("[\\D]".toRegex(), "") == j.toString()
                                ) {
                                    if (!secretsList!![j - 1]) continue
                                    secretsList!![j - 1] = false
                                    allSecretsMap.replace(roomName, secretsList)
                                    DungeonRooms.logger.info(
                                        "DungeonRooms: ${entity.commandSenderEntity.name} picked up ${
                                            StringUtils.stripControlCodes(
                                                name
                                            )
                                        } from a ${secretsObject["category"].asString} secret, turning off waypoint for secret #$j"
                                    )
                                    return@forEverySecretInRoom
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    // Disable waypoint within 4 blocks away on sneak
    @SubscribeEvent
    fun onKey(event: InputEvent.KeyInputEvent?) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled || !DRMConfig.sneakToDisable) return
        val player = Minecraft.getMinecraft().thePlayer
        if (FMLClientHandler.instance().client.gameSettings.keyBindSneak.isPressed) {
            val preupdate = lastSneakTime
            lastSneakTime = System.currentTimeMillis()
            if (System.currentTimeMillis() - preupdate < 500) { // check for two taps in under half a second

                DungeonRooms.instance.forEverySecretInRoom { (secretsObject, roomName) ->
                    if (secretsObject["category"].asString == "chest" || secretsObject["category"].asString == "wither" || secretsObject["category"].asString == "item" || secretsObject["category"].asString == "bat") {
                        val relative =
                            BlockPos(secretsObject["x"].asInt, secretsObject["y"].asInt, secretsObject["z"].asInt)
                        val pos = MapUtils.relativeToActual(
                            relative,
                            DungeonRooms.instance.roomDetection.roomDirection,
                            DungeonRooms.instance.roomDetection.roomCorner!!
                        )
                        if (player.getDistanceSq(pos) <= 16.0) {
                            for (j in 1..secretCount) {
                                if (secretsObject["secretName"].asString.substring(0, 2)
                                        .replace("[\\D]".toRegex(), "") == j.toString()
                                ) {
                                    if (!secretsList!![j - 1]) continue
                                    secretsList!![j - 1] = false
                                    allSecretsMap.replace(roomName, secretsList)
                                    DungeonRooms.logger.info("DungeonRooms: Player sneaked near " + secretsObject["category"].asString + " secret, turning off waypoint for secret #" + j)
                                    return@forEverySecretInRoom
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var lastSneakTime: Long = 0
    companion object {

        var allFound = false

        @JvmField
        var secretCount: Int = 0
        var completedSecrets = 0

        @JvmField
        var allSecretsMap: MutableMap<String, List<Boolean>?> = HashMap()

        @JvmField
        var secretsList: MutableList<Boolean>? = ArrayList(BooleanArray(10).toMutableList())

    }
}