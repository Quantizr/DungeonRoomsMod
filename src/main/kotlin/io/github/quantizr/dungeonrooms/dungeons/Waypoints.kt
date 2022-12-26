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
    private var completedSecrets = 0

    var allFound = false
    var secretCount = 0
    var allSecretsMap: MutableMap<String, List<Boolean>?> = HashMap()
    var secretsList: MutableList<Boolean> = ArrayList(BooleanArray(10).toList())

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!DRMConfig.waypointsEnabled) return
        if (DRMConfig.practiceModeOn && !DRMConfig.practiceModeKeyBind.isActive) return
        if (DRMConfig.disableWhenAllFound && allFound) return

        val (_, secretList) = DungeonRooms.instance.getJsonSecretList() ?: return
        val viewer = Minecraft.getMinecraft().renderViewEntity
        frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ)

        secretList.stream()
            // don't render fairy souls
            .filter { it.category != "fairysoul" }

            // make sure the secret is not done
            .filter { secret ->
                val nmbr = getSecretNumber(secret.secretName)
                nmbr in (1..secretCount) && secretsList[nmbr - 1]
            }

            // make sure we are looking at it
            .filter {
                val pos = getSecretPos(it.x, it.y, it.z)
                frustum.isBoxInFrustum(
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble(),
                    (pos.x + 1).toDouble(),
                    255.0,
                    (pos.z + 1).toDouble()
                )
            }

            // render the beacon-text
            .forEach {
                val pos = getSecretPos(it.x, it.y, it.z)
                val color = when (it.category) {
                    "entrance" -> {
                        if (!DRMConfig.showEntrance) return@forEach
                        Color(0, 255, 0)
                    }

                    "superboom" -> {
                        if (!DRMConfig.showSuperboom) return@forEach
                        Color(255, 0, 0)
                    }

                    "chest" -> {
                        if (!DRMConfig.showSecrets) return@forEach
                        Color(2, 213, 250)
                    }

                    "item" -> {
                        if (!DRMConfig.showSecrets) return@forEach
                        Color(2, 64, 250)
                    }

                    "bat" -> {
                        if (!DRMConfig.showSecrets) return@forEach
                        Color(142, 66, 0)
                    }

                    "wither" -> {
                        if (!DRMConfig.showSecrets) return@forEach
                        Color(30, 30, 30)
                    }

                    "lever" -> {
                        if (!DRMConfig.showSecrets) return@forEach
                        Color(250, 217, 2)
                    }

                    "fairysoul" -> {
                        if (!DRMConfig.showFairySouls) return@forEach
                        Color(255, 85, 255)
                    }

                    "stonk" -> {
                        if (!DRMConfig.showStonk) return@forEach
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
                if (DRMConfig.showBeacon && distSq > 5 * 5) {
                    WaypointUtils.renderBeaconBeam(
                        x,
                        y + 1,
                        z,
                        color.rgb,
                        0.25f,
                        event.partialTicks
                    )
                }
                if (DRMConfig.showWaypointText) {
                    WaypointUtils.renderWaypointText(
                        it.secretName,
                        pos.up(2),
                        event.partialTicks
                    )
                }
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


        val (roomId, secretList) = DungeonRooms.instance.getJsonSecretList() ?: return

        secretList.stream()
            .filter { it.category == "chest" || it.category == "wither" }
            .filter { getSecretPos(it.x, it.y, it.z) == event.pos }
            .filter { getSecretNumber(it.secretName) in (1..secretCount) }
            .forEach {
                val nmbr = getSecretNumber(it.secretName)
                secretsList[nmbr - 1] = false
                allSecretsMap.replace(roomId, secretsList)
                DungeonRooms.logger.info("DungeonRooms: Detected ${it.category} click, turning off waypoint for secret #$nmbr")
            }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled) return
        if (DRMConfig.disableWhenAllFound && allFound) return

        if (event.packet !is S0DPacketCollectItem) return
        val packet = event.packet as S0DPacketCollectItem

        val mc = Minecraft.getMinecraft()
        var entity = mc.theWorld.getEntityByID(packet.collectedItemEntityID) ?: return
        if (entity !is EntityItem) return
        val item = entity // smart casting shat itself and I have to use this
        entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
        val name = item.entityItem.displayName
        if (!name.contains("Decoy") &&
            !name.contains("Defuse Kit") &&
            !name.contains("Dungeon Chest Key") &&
            !name.contains("Healing VIII") &&
            !name.contains("Inflatable Jerry") &&
            !name.contains("Spirit Leap") &&
            !name.contains("Training Weights") &&
            !name.contains("Trap") &&
            !name.contains("Treasure Talisman")
        ) {
            return
        }
        if (entity.commandSenderEntity.name != mc.thePlayer.name) {
            // Do nothing if someone else picks up the item in order to follow Hypixel rules (laugh haha lol)
            return
        }

        val (roomId, secretList) = DungeonRooms.instance.getJsonSecretList() ?: return

        secretList.stream()
            .filter { it.category == "item" || it.category == "bat" } // only bats and items
            .filter { entity.getDistanceSq(getSecretPos(it.x, it.y, it.z)) <= 36 } // within 6 blocks

            // map the secret name to the secret number, so we don't recalc it every time
            .map { secret ->
                Pair(secret, getSecretNumber(secret.secretName))
            }

            // get current secret
            .filter { (_, nmbr) ->
                nmbr in (1..secretCount)
            }

            // check if secret is already found
            .filter { (_, nmbr) ->
                secretsList[nmbr - 1]
            }

            // finish the secret
            .forEach { (secret, nmbr) ->
                secretsList[nmbr - 1] = false
                allSecretsMap.replace(roomId, secretsList)
                DungeonRooms.logger.info(
                    "DungeonRooms: ${entity.commandSenderEntity.name} picked up ${
                        StringUtils.stripControlCodes(
                            name
                        )
                    } from a ${secret.category} secret, turning off waypoint for secret #$nmbr"
                )
                return@forEach
            }

    }

    private fun getSecretPos(x: Int, y: Int, z: Int): BlockPos {
        return MapUtils.relativeToActual(
            BlockPos(x, y, z),
            DungeonRooms.instance.roomDetection.roomDirection,
            DungeonRooms.instance.roomDetection.roomCorner!!
        )
    }


    private fun getSecretNumber(secretName: String): Int {
        return secretName.substring(0, 2).replace("\\D".toRegex(), "").toInt()
    }

    private var lastSneakTime: Long = 0

    // Disable waypoint within 4 blocks away on sneak
    @SubscribeEvent
    fun onKey(event: InputEvent.KeyInputEvent?) {
        if (!Utils.inCatacombs || !DRMConfig.waypointsEnabled || !DRMConfig.sneakToDisable) return
        if (!FMLClientHandler.instance().client.gameSettings.keyBindSneak.isPressed) return

        val tmp = lastSneakTime // use temp value cuz we want to update the time at the start and not the end
        lastSneakTime = System.currentTimeMillis()

        // check for two taps in under half a second
        if ((System.currentTimeMillis() - tmp) >= 500) return

        val player = Minecraft.getMinecraft().thePlayer


        val (roomId, secretList) = DungeonRooms.instance.getJsonSecretList() ?: return

        secretList.stream()
            .filter { it.category == "chest" || it.category == "wither" || it.category == "item" || it.category == "bat" }
            .filter { player.getDistanceSq(getSecretPos(it.x, it.y, it.z)) <= 16 } // within 4 blocks

            // map the secret name to the secret number, so we don't recalc it every time
            .map { secret -> Pair(secret, getSecretNumber(secret.secretName)) }

            // get current secret
            .filter { (_, nmbr) ->
                nmbr in (1..secretCount)
            }

            // check if secret is already found
            .filter { (_, nmbr) ->
                secretsList[nmbr - 1]
            }

            // finish the secret
            .forEach { (secret, nmbr) ->
                secretsList[nmbr - 1] = false
                allSecretsMap.replace(roomId, secretsList)
                DungeonRooms.logger.info("DungeonRooms: Player sneaked near ${secret.category} secret, turning off waypoint for secret #$nmbr")
                return@forEach
            }
    }

}