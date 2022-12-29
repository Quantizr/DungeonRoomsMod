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
package io.github.quantizr.dungeonrooms

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.quantizr.dungeonrooms.commands.RoomCommand
import io.github.quantizr.dungeonrooms.dungeons.DungeonManager
import io.github.quantizr.dungeonrooms.dungeons.RoomDetection
import io.github.quantizr.dungeonrooms.dungeons.Waypoints
import io.github.quantizr.dungeonrooms.dungeons.data.meta.SecretMetaData
import io.github.quantizr.dungeonrooms.gui.WaypointsGUI
import io.github.quantizr.dungeonrooms.handlers.OpenLink.checkForLink
import io.github.quantizr.dungeonrooms.handlers.PacketHandler
import io.github.quantizr.dungeonrooms.handlers.TextRenderer.drawText
import io.github.quantizr.dungeonrooms.test.PathfindTest
import io.github.quantizr.dungeonrooms.utils.BlockCache
import io.github.quantizr.dungeonrooms.utils.Utils
import io.github.quantizr.dungeonrooms.utils.Utils.checkForCatacombs
import io.github.quantizr.dungeonrooms.utils.Utils.checkForSkyblock
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Mod(modid = DungeonRooms.MODID, version = DungeonRooms.VERSION)
class DungeonRooms {
    val mc: Minecraft = Minecraft.getMinecraft()
    val motd: MutableList<String> = ArrayList()
    var textToDisplay: List<String> = emptyList()
    private var tickAmount = 1

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        ClientCommandHandler.instance.registerCommand(RoomCommand())

        //initialize logger
        logger = LogManager.getLogger(instance::class.java)
//        if(debug) Utils.setLogLevel(LogManager.getLogger(DungeonRooms::class.java), Level.DEBUG)
    }

    val roomDetection = RoomDetection()
    val roomDataLoader = RoomDataLoader()
    val dungeonManager = DungeonManager()
    val waypoints = Waypoints()


    private fun isFirstLaunch(): Boolean{
        val controlfile = File(System.getProperty("user.dir") + File.separator + "drmFirstLaunchControlFile")

        if(!controlfile.exists()){
            controlfile.createNewFile()
            return true
        }
        return false
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {

        DRMConfig.init()
        roomDataLoader.startAsyncLoad()
        firstLogin = isFirstLaunch()

        //register classes
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(BlockCache)

        MinecraftForge.EVENT_BUS.register(ChatTransmitter())
        MinecraftForge.EVENT_BUS.register(dungeonManager)
        MinecraftForge.EVENT_BUS.register(roomDetection)
        MinecraftForge.EVENT_BUS.register(waypoints)
        if (DRMConfig.debug) MinecraftForge.EVENT_BUS.register(PathfindTest())

        roomDataLoader.blockTillLoad()

        Thread {
            try {
                logger.info("DungeonRooms: Checking for updates...")
                val gson = Gson()
                val thaobject = gson.fromJson(
                    IOUtils.toString(URL("https://api.github.com/repos/Quantizr/DungeonRoomsMod/releases/latest")),
                    JsonObject::class.java
                )
                val latestTag = thaobject.get("tag_name").asString
                val currentVersion = DefaultArtifactVersion(VERSION)
                val latestVersion = DefaultArtifactVersion(latestTag.substring(1))
                if (currentVersion < latestVersion) {
                    val releaseURL = "https://github.com/Quantizr/DungeonRoomsMod/releases/latest"
                    val update = ChatComponentText("${EnumChatFormatting.GREEN}${EnumChatFormatting.BOLD}  [UPDATE]  ")
                    update.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)
                    ChatTransmitter.addToQueue(
                        ChatComponentText("${EnumChatFormatting.RED}Dungeon Rooms Mod is outdated. Please update to $latestTag.").appendSibling(
                            update
                        )
                    )
                } else {
                    logger.info("DungeonRooms: No update found")
                }

                logger.info("DungeonRooms: Getting MOTD...")
                val motdText =
                    IOUtils.toString(URL("https://gist.githubusercontent.com/Quantizr/01aca53e61cef5dfd08989fec600b204/raw/"))
                motd.addAll(motdText.split(System.lineSeparator()))
                logger.info("DungeonRooms: MOTD has been checked")

            } catch (e: IOException) {
                ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: An error has occured. See logs for more details.")
                e.printStackTrace()
            } catch (e: InterruptedException) {
                ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: An error has occured. See logs for more details.")
                e.printStackTrace()
            }
        }.start()
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent?) {
        usingSBPSecrets = Loader.isModLoaded("sbp")
        logger.info("DungeonRooms: SBP Dungeon Secrets detection: $usingSBPSecrets")
    }

    fun getJsonSecretList(): Pair<String, List<SecretMetaData>>? {
        if (roomDetection.roomName == "undefined") return null

        val room = roomDataLoader.roomData[roomDetection.roomName] ?: return null

        return Pair(room.id, room.secrets.toList())
    }

    /**
     * Modified from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    @SubscribeEvent
    fun onServerConnect(event: ClientConnectedToServerEvent) {
        if (mc.currentServerData == null) return
        if (!mc.currentServerData.serverIP.lowercase().contains("hypixel.")) return

        logger.info("DungeonRooms: Connecting to Hypixel...")
        // Packets are used in this mod solely to detect when the player picks up an item. No packets are modified or created.
        event.manager.channel().pipeline().addBefore("packet_handler", "drm_packet_handler", PacketHandler())
        logger.info("DungeonRooms: Packet Handler added")
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickAmount++
        if (tickAmount % 20 == 0) {
            if (mc.thePlayer != null) {
                checkForSkyblock()
                checkForCatacombs()
                tickAmount = 0
            }
        }
    }

    @SubscribeEvent
    fun onKey(event: InputEvent.KeyInputEvent?) {
        when {
            DRMConfig.openSecretImages.isActive -> {
                if (!Utils.inCatacombs) {
                    ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: Use this hotkey inside of a dungeon room")
                    return
                }
                when (DRMConfig.imageHotkeyOpen) {
                    0 -> checkForLink("gui")
                    1 -> checkForLink("dsg")
                    2 -> checkForLink("sbp")
                    else -> ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: hotkeyOpen config value improperly set, do \"/room set <gui | dsg | sbp>\" to change the value")
                }
            }
            DRMConfig.waypointGuiKey.isActive -> {
                mc.addScheduledTask { mc.displayGuiScreen(WaypointsGUI()) }
            }
            DRMConfig.practiceModeKeyBind.isActive -> {
                if (DRMConfig.waypointsEnabled && !DRMConfig.practiceModeOn) {
                    ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: Run \"/room toggle practice\" to enable Practice Mode.")

                } else if (!DRMConfig.waypointsEnabled && DRMConfig.practiceModeOn) {
                    ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: Waypoints must be enabled for Practice Mode to work.")

                }
            }
        }
    }

    @SubscribeEvent
    fun renderPlayerInfo(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        if (!Utils.inSkyblock) return
        if (textToDisplay.isNotEmpty()) {
            val scaledResolution = ScaledResolution(mc)
            var y = 0
            for (line in textToDisplay) {
                val roomStringWidth = mc.fontRendererObj.getStringWidth(line)
                drawText(
                    mc, line, scaledResolution.scaledWidth * DRMConfig.textLocX / 100 - roomStringWidth / 2,
                    scaledResolution.scaledHeight * DRMConfig.textLocY / 100 + y, 1.0, true
                )
                y += mc.fontRendererObj.FONT_HEIGHT
            }
        }
    }


    companion object {
        @Mod.Instance
        @JvmStatic
        lateinit var instance: DungeonRooms
            private set

        const val MODID = "@ID@"
        const val VERSION = "@VER@"
        lateinit var logger: Logger
        var usingSBPSecrets = false
            private set

        var firstLogin = false
    }
}