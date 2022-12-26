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
package io.github.quantizr.dungeonrooms.commands

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.ChatTransmitter
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.dungeons.DungeonManager
import io.github.quantizr.dungeonrooms.dungeons.DungeonRunStage
import io.github.quantizr.dungeonrooms.gui.WaypointsGUI
import io.github.quantizr.dungeonrooms.handlers.ConfigHandler
import io.github.quantizr.dungeonrooms.handlers.OpenLink
import io.github.quantizr.dungeonrooms.utils.MapUtils
import io.github.quantizr.dungeonrooms.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class RoomCommand : CommandBase() {
    override fun getCommandName(): String {
        return "room"
    }

    override fun getCommandUsage(arg0: ICommandSender): String {
        return "/$commandName"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("drm")
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String>? {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "waypoints", "move", "toggle", "set", "discord")
        }
        if (args.size > 1) {
            if (args[0].equals("toggle", ignoreCase = true)) {
                return getListOfStringsMatchingLastWord(
                    args,
                    "help",
                    "gui",
                    "waypointtext",
                    "waypointboundingbox",
                    "waypointbeacon"
                )
            } else if (args[0].equals("set", ignoreCase = true)) {
                return getListOfStringsMatchingLastWord(args, "gui", "dsg", "sbp")
            }
        }
        return null
    }

    override fun processCommand(arg0: ICommandSender, arg1: Array<String>) {
        val mc = Minecraft.getMinecraft()
        val player = arg0 as EntityPlayer
        if (arg1.isEmpty()) {
            if (!Utils.inCatacombs) {
                ChatTransmitter.addToQueue(
                    "${EnumChatFormatting.RED}Dungeon Rooms: Use this command in dungeons or run \"/room help\" for additional options"
                )

            } else {
                if (DungeonRooms.instance.dungeonManager.gameStage == DungeonRunStage.RoomClear) {
                    for (line in DungeonRooms.textToDisplay!!) {
                        player.addChatMessage(ChatComponentText(line))
                    }
                    ChatTransmitter.addToQueue(
                        "${EnumChatFormatting.GREEN}Dungeon Rooms: You can also run \"/room help\" for additional options"
                    )

                } else {
                    ChatTransmitter.addToQueue(
                        "${EnumChatFormatting.RED}Dungeon Rooms: Use this command while clearing rooms or run \"/room help\" for additional options"
                    )

                }
            }
        } else {
            when (arg1[0].lowercase()) {
                "help" -> ChatTransmitter.addToQueue(
                    """
                    ${EnumChatFormatting.GOLD}Dungeon Rooms Mod Version ${DungeonRooms.VERSION}
                    ${EnumChatFormatting.DARK_PURPLE}Hotkeys: (Configurable in Controls Menu)
                    ${EnumChatFormatting.AQUA} ${DRMConfig.waypointGuiKey.display}${EnumChatFormatting.WHITE} - Opens Secret Waypoints configuration GUI
                    ${EnumChatFormatting.AQUA} ${DRMConfig.openSecretImages.display}${EnumChatFormatting.WHITE} - (old) Opens images of secret locations
                    ${EnumChatFormatting.AQUA} ${DRMConfig.practiceModeKeyBind.display}${EnumChatFormatting.WHITE} - View waypoints in Practice Mode ("/room toggle practice")
                    ${EnumChatFormatting.DARK_PURPLE}Commands:
                    ${EnumChatFormatting.AQUA} /room${EnumChatFormatting.WHITE} - Tells you in chat what room you are standing in.
                    ${EnumChatFormatting.AQUA} /room help${EnumChatFormatting.WHITE} - Displays this message.
                    ${EnumChatFormatting.AQUA} /room waypoints${EnumChatFormatting.WHITE} - Opens Secret Waypoints config GUI, alternatively can be opened with hotkey
                    ${EnumChatFormatting.AQUA} /room move <x> <y>${EnumChatFormatting.WHITE} - Moves the GUI room name text to a coordinate. <x> and <y> are numbers between 0 and 100. Default is 50 for <x> and 5 for <y>.
                    ${EnumChatFormatting.AQUA} /room toggle [argument]${EnumChatFormatting.WHITE} - Run "/room toggle help" for full list of toggles.
                    ${EnumChatFormatting.AQUA} /room set <gui | dsg | sbp>${EnumChatFormatting.WHITE} - Configure whether the hotkey opens the selector GUI or directly goes to DSG/SBP.
                    ${EnumChatFormatting.AQUA} /room discord${EnumChatFormatting.WHITE} - Opens the Discord invite for this mod in your browser.
                    """.trimIndent()
                )

                "gui", "open" -> {
                    if (!Utils.inCatacombs) {
                        ChatTransmitter.addToQueue(
                            "${EnumChatFormatting.RED}Dungeon Rooms: Use this command in dungeons"
                        )
                        return
                    }
                    OpenLink.checkForLink("gui")
                }

                "dsg" -> {
                    if (!Utils.inCatacombs) {
                        ChatTransmitter.addToQueue(
                            "${EnumChatFormatting.RED}Dungeon Rooms: Use this command in dungeons"
                        )
                        return
                    }
                    OpenLink.checkForLink("dsg")
                }

                "sbp" -> {
                    if (!Utils.inCatacombs) {
                        ChatTransmitter.addToQueue(
                            "${EnumChatFormatting.RED}Dungeon Rooms: Use this command in dungeons"
                        )
                        return
                    }
                    OpenLink.checkForLink("sbp")
                }

                "set" -> when (arg1[1].lowercase()) {
                    "gui" -> {
                        DRMConfig.imageHotkeyOpen = 0
                        ChatTransmitter.addToQueue("Dungeon Rooms: Hotkey has been set to open: GUI")
                    }

                    "dsg" -> {
                        DRMConfig.imageHotkeyOpen = 1
                        ChatTransmitter.addToQueue("Dungeon Rooms: Hotkey has been set to open: DSG")
                    }

                    "sbp" -> {
                        DRMConfig.imageHotkeyOpen = 2
                        ChatTransmitter.addToQueue("Dungeon Rooms: Hotkey has been set to open: SBP")
                    }

                    else -> ChatTransmitter.addToQueue(
                        "${EnumChatFormatting.RED}Dungeon Rooms: Valid options are <gui | dsg | sbp>"
                    )

                }

                "wp", "waypoint", "waypoints" -> Thread { mc.addScheduledTask { mc.displayGuiScreen(WaypointsGUI()) } }.start()
                "move" -> {
                    DRMConfig.textLocX = arg1[1].toInt()
                    DRMConfig.textLocY = arg1[2].toInt()
                    ChatTransmitter.addToQueue("Dungeon Rooms: Room GUI has been moved to ${arg1[1]}, ${arg1[2]}")
                }

                "toggle" -> {
                    val toggleHelp = """
                    ${EnumChatFormatting.GOLD} Dungeon Rooms Mod Toggle Commands:
                    ${EnumChatFormatting.AQUA} /room toggle gui${EnumChatFormatting.WHITE} - Toggles displaying current room in gui.
                    ${EnumChatFormatting.AQUA} /room toggle motd${EnumChatFormatting.WHITE} - Toggles displaying the Welcome/MOTD message at the start of a dungeon run.
                    ${EnumChatFormatting.AQUA} /room toggle practice${EnumChatFormatting.WHITE} - Toggles Practice Mode, where waypoints are only displayed when holding down ${
                        DRMConfig.practiceModeKeyBind.display
                    }".${EnumChatFormatting.AQUA} /room toggle waypoints${EnumChatFormatting.WHITE} - Toggles Waypoints, does the same thing as pressing "${
                        DRMConfig.waypointGuiKey.display
                    }" then clicking "Waypoints".
                    ${EnumChatFormatting.AQUA} /room toggle waypointtext${EnumChatFormatting.WHITE} - Toggles displaying waypoint names above waypoints.
                    ${EnumChatFormatting.AQUA} /room toggle waypointboundingbox${EnumChatFormatting.WHITE} - Toggles displaying the bounding box on waypoints.
                    ${EnumChatFormatting.AQUA} /room toggle waypointbeacon${EnumChatFormatting.WHITE} - Toggles displaying the beacon above waypoints.
                    """.trimIndent()
                    if (arg1.size == 1) {
                        ChatTransmitter.addToQueue(toggleHelp)
                    } else {
                        when (arg1[1].lowercase()) {
                            "gui" -> {
                                DRMConfig.guiToggled = !DRMConfig.guiToggled
                                ChatTransmitter.addToQueue("Dungeon Rooms: Display room names in GUI has been set to: " + DRMConfig.guiToggled)
                            }

                            "welcome", "motd" -> {
                                DRMConfig.motdToggled = !DRMConfig.motdToggled
                                ChatTransmitter.addToQueue("Dungeon Rooms: Display Welcome/MOTD has been set to: " + DRMConfig.motdToggled)
                            }

                            "practice", "practicemode" -> {
                                DRMConfig.practiceModeOn = !DRMConfig.practiceModeOn
                                if (DRMConfig.practiceModeOn) {
                                    ChatTransmitter.addToQueue(
                                        """
                                        §eDungeon Rooms: Practice Mode has been enabled.
                                        §e Waypoints will only show up while you are pressing "${
                                            DRMConfig.practiceModeKeyBind.display
                                        }".
                                        §r (Hotkey is configurable in Minecraft Controls menu)
                                        """.trimIndent()
                                    )

                                } else {
                                    ChatTransmitter.addToQueue("§eDungeon Rooms: Practice Mode has been disabled.")
                                }
                            }

                            "waypoint", "waypoints" -> {
                                DRMConfig.waypointsEnabled = !DRMConfig.waypointsEnabled
                                if (DRMConfig.waypointsEnabled) {
                                    ChatTransmitter.addToQueue("§eDungeon Rooms: Waypoints will now automatically show up when you enter a new dungeon room.")
                                } else {
                                    ChatTransmitter.addToQueue("§eDungeon Rooms: Waypoints have been disabled.")
                                }
                            }

                            "text", "waypointtext" -> {
                                DRMConfig.showWaypointText = !DRMConfig.showWaypointText
                                ChatTransmitter.addToQueue("Dungeon Rooms: Show Waypoint Text has been set to: " + DRMConfig.showWaypointText)
                            }

                            "boundingbox", "waypointboundingbox" -> {
                                DRMConfig.showBoundingBox = !DRMConfig.showBoundingBox
                                ChatTransmitter.addToQueue("Dungeon Rooms: Show Waypoint Bounding Box has been set to: " + DRMConfig.showBoundingBox)
                            }

                            "beacon", "waypointbeacon" -> {
                                DRMConfig.showBeacon = !DRMConfig.showBeacon
                                ChatTransmitter.addToQueue("Dungeon Rooms: Show Waypoint Beacon has been set to: " + DRMConfig.showBeacon)
                            }

                            "override" -> {
                                Utils.dungeonOverride = !Utils.dungeonOverride
                                ChatTransmitter.addToQueue("Dungeon Rooms: Force inCatacombs has been set to: " + Utils.dungeonOverride)
                            }

                            "help" -> ChatTransmitter.addToQueue(toggleHelp)
                            else -> ChatTransmitter.addToQueue(toggleHelp)
                        }
                    }
                }

                "reload" -> {
                    ConfigHandler.reloadConfig()
                    ChatTransmitter.addToQueue("Dungeon Rooms: Reloaded config file")
                }

                "discord" -> try {
                    ChatTransmitter.addToQueue("Dungeon Rooms: Opening Dungeon Rooms Discord invite in browser...")
                    Desktop.getDesktop().browse(URI("https://discord.gg/7B5RbsArYK"))
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }

                "relative" -> if (DungeonRooms.instance.roomDetection.roomDirection != "undefined" && DungeonRooms.instance.roomDetection.roomCorner != null) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                        val relativePos = MapUtils.actualToRelative(
                            mc.objectMouseOver.blockPos,
                            DungeonRooms.instance.roomDetection.roomDirection,
                            DungeonRooms.instance.roomDetection.roomCorner!!
                        )
                        ChatTransmitter.addToQueue("Dungeon Rooms: You are looking at relative blockPos: $relativePos")
                    }
                } else {
                    ChatTransmitter.addToQueue("Dungeon Rooms: Unable to get relative blockPos at this time.")
                }

                "json" -> {
                    if (!Utils.inCatacombs && DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.RoomClear && DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.Boss) {
                        ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: Use this command in dungeons")
                        return
                    }
                    val room = DungeonRooms.instance.roomDataLoader.roomData[DungeonRooms.instance.roomDetection.roomName]

                    if(room != null){
                        ChatTransmitter.addToQueue(room.toString())
                    }
                }

                "roominfo" -> {
                    if (!Utils.inCatacombs || DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.RoomClear) {
                        ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: Not in room clearing phase of dungeons")
                    }
                    if (DungeonManager.entranceMapCorners != null) {
                        ChatTransmitter.addToQueue("dev: entranceMapCorners = ${listOf(DungeonManager.entranceMapCorners)}")

                    } else {
                        ChatTransmitter.addToQueue("dev: entranceMapCorners = null")
                    }
                    if (DungeonManager.entrancePhysicalNWCorner != null) {
                        ChatTransmitter.addToQueue("dev: entrancePhysicalNWCorner = ${DungeonManager.entrancePhysicalNWCorner}")
                    } else {
                        ChatTransmitter.addToQueue("dev: entrancePhysicalNWCorner = null")
                    }
                    ChatTransmitter.addToQueue("dev: currentMapSegments = ${DungeonRooms.instance.roomDetection.currentMapSegments}")
                    ChatTransmitter.addToQueue("dev: currentPhysicalSegments = ${DungeonRooms.instance.roomDetection.currentPhysicalSegments}")
                    ChatTransmitter.addToQueue("dev: roomName = ${DungeonRooms.instance.roomDetection.roomName}")
                    ChatTransmitter.addToQueue("dev: roomSize = ${DungeonRooms.instance.roomDetection.roomSize}")
                    ChatTransmitter.addToQueue("dev: roomColor = ${DungeonRooms.instance.roomDetection.roomColor}")
                    ChatTransmitter.addToQueue("dev: roomCategory = ${DungeonRooms.instance.roomDetection.roomCategory}")
                    ChatTransmitter.addToQueue("dev: roomDirection = ${DungeonRooms.instance.roomDetection.roomDirection}")
                    if (DungeonRooms.instance.roomDetection.roomCorner != null) {
                        ChatTransmitter.addToQueue("dev: roomCorner = " + DungeonRooms.instance.roomDetection.roomCorner)
                    } else {
                        ChatTransmitter.addToQueue("dev: roomCorner = null")
                    }
                }

                "blocksused" -> {
                    if (!Utils.inCatacombs || DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.RoomClear) {
                        player.addChatMessage(
                            ChatComponentText("${EnumChatFormatting.RED}Dungeon Rooms: Not in room clearing phase of dungeons")
                        )
                    }
                    ChatTransmitter.addToQueue(DungeonRooms.instance.roomDetection.blocksUsed.toString())
                }

                "add" -> {
                    val world: World = mc.theWorld
                    if (!Utils.inCatacombs || DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.RoomClear || DungeonRooms.instance.roomDetection.roomDirection == "undefined" || DungeonRooms.instance.roomDetection.roomCorner == null) {
                        player.addChatMessage(
                            ChatComponentText(
                                "${EnumChatFormatting.RED}Dungeon Rooms: Current dungeon room is undefined"
                            )
                        )
                        return
                    }
                    when (arg1[1].lowercase()) {
                        "chest" -> if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                            val viewingPos = MapUtils.actualToRelative(
                                mc.objectMouseOver.blockPos,
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            if (world.getBlockState(mc.objectMouseOver.blockPos).block === Blocks.chest) {
                                player.addChatMessage(
                                    ChatComponentText(
                                        """{
                                          "secretName":"# - Chest",
                                          "category":"chest",
                                          "x":${viewingPos.x},
                                          "y":${viewingPos.y},
                                          "z":${viewingPos.z}
                                        }"""
                                    )
                                )
                                Toolkit.getDefaultToolkit()
                                    .systemClipboard
                                    .setContents(
                                        StringSelection(
                                            """{
                                                "secretName":"# - Chest",
                                                "category":"chest",
                                                "x":${viewingPos.x},
                                                "y":${viewingPos.y},
                                                "z":${viewingPos.z}
                                                }""".trimIndent()
                                        ),
                                        null
                                    )
                            } else {
                                ChatTransmitter.addToQueue("You are not looking at a Chest Secret")
                            }
                        } else {
                            ChatTransmitter.addToQueue("You are not looking at anything")
                        }

                        "wither" -> if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                            val viewingPos = MapUtils.actualToRelative(
                                mc.objectMouseOver.blockPos,
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            if (world.getBlockState(mc.objectMouseOver.blockPos).block === Blocks.skull) {
                                player.addChatMessage(
                                    ChatComponentText(
                                        """{  "secretName":"# - Wither Essence",  "category":"wither",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                    )
                                )
                                Toolkit.getDefaultToolkit()
                                    .systemClipboard
                                    .setContents(
                                        StringSelection(
                                            """{  "secretName":"# - Wither Essence",  "category":"wither",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                        ),
                                        null
                                    )
                            } else {
                                ChatTransmitter.addToQueue("You are not looking at a Wither Essence Secret")
                            }
                        } else {
                            ChatTransmitter.addToQueue("You are not looking at anything")
                        }

                        "superboom" -> if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                            val viewingPos = MapUtils.actualToRelative(
                                mc.objectMouseOver.blockPos,
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            if (world.getBlockState(mc.objectMouseOver.blockPos).block === Blocks.stonebrick) {
                                player.addChatMessage(
                                    ChatComponentText(
                                        """{  "secretName":"# - Superboom",  "category":"superboom",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                    )
                                )
                                Toolkit.getDefaultToolkit()
                                    .systemClipboard
                                    .setContents(
                                        StringSelection(
                                            """{  "secretName":"# - Superboom",  "category":"superboom",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                        ),
                                        null
                                    )
                            } else {
                                ChatTransmitter.addToQueue("You are not looking at a Superboom entrance")
                            }
                        } else {
                            ChatTransmitter.addToQueue("You are not looking at anything")
                        }

                        "lever" -> if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                            val viewingPos = MapUtils.actualToRelative(
                                mc.objectMouseOver.blockPos,
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            if (world.getBlockState(mc.objectMouseOver.blockPos).block === Blocks.lever) {
                                player.addChatMessage(
                                    ChatComponentText(
                                        """{  "secretName":"# - Lever",  "category":"lever",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                    )
                                )
                                Toolkit.getDefaultToolkit()
                                    .systemClipboard
                                    .setContents(
                                        StringSelection(
                                            """{  "secretName":"# - Lever",  "category":"lever",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                        ),
                                        null
                                    )
                            } else {
                                ChatTransmitter.addToQueue("You are not looking at a Lever")
                            }
                        } else {
                            ChatTransmitter.addToQueue("You are not looking at anything")
                        }

                        "fairysoul" -> if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.blockPos != null) {
                            val viewingPos = MapUtils.actualToRelative(
                                mc.objectMouseOver.blockPos.up(1),
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            if (world.getBlockState(mc.objectMouseOver.blockPos.up(1)).block === Blocks.air) {
                                ChatTransmitter.addToQueue(
                                    """{  "secretName":"Fairy Soul",  "category":"fairysoul",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                )

                                Toolkit.getDefaultToolkit()
                                    .systemClipboard
                                    .setContents(
                                        StringSelection(
                                            """{  "secretName":"Fairy Soul",  "category":"fairysoul",  "x":${viewingPos.x},  "y":${viewingPos.y},  "z":${viewingPos.z}}"""
                                        ),
                                        null
                                    )
                            } else {
                                ChatTransmitter.addToQueue("You are not looking at the block below a Fairy Soul")
                            }
                        } else {
                            ChatTransmitter.addToQueue("You are not looking at anything")
                        }

                        "item" -> {
                            val playerPos = MapUtils.actualToRelative(
                                BlockPos(player.posX, player.posY, player.posZ),
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            ChatTransmitter.addToQueue(
                                """{  "secretName":"# - Item",  "category":"item",  "x":${playerPos.x},  "y":${playerPos.y},  "z":${playerPos.z}}"""
                            )

                            Toolkit.getDefaultToolkit()
                                .systemClipboard
                                .setContents(
                                    StringSelection(
                                        """{  "secretName":"# - Item",  "category":"item",  "x":${playerPos.x},  "y":${playerPos.y},  "z":${playerPos.z}}"""
                                    ),
                                    null
                                )
                        }

                        "entrance" -> {
                            val entrancePos = MapUtils.actualToRelative(
                                BlockPos(player.posX, player.posY + 1, player.posZ),
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            ChatTransmitter.addToQueue(
                                """{  "secretName":"# - Entrance",  "category":"entrance",  "x":${entrancePos.x},  "y":${entrancePos.y},  "z":${entrancePos.z}}"""
                            )

                            Toolkit.getDefaultToolkit()
                                .systemClipboard
                                .setContents(
                                    StringSelection(
                                        """{  "secretName":"# - Entrance",  "category":"entrance",  "x":${entrancePos.x},  "y":${entrancePos.y},  "z":${entrancePos.z}}"""
                                    ),
                                    null
                                )
                        }

                        "bat" -> {
                            val batPos = MapUtils.actualToRelative(
                                BlockPos(player.posX, player.posY + 1, player.posZ),
                                DungeonRooms.instance.roomDetection.roomDirection,
                                DungeonRooms.instance.roomDetection.roomCorner!!
                            )
                            ChatTransmitter.addToQueue(
                                """{  "secretName":"# - Bat",  "category":"bat",  "x":${batPos.x},  "y":${batPos.y},  "z":${batPos.z}}"""
                            )

                            Toolkit.getDefaultToolkit()
                                .systemClipboard
                                .setContents(
                                    StringSelection(
                                        """{  "secretName":"# - Bat",  "category":"bat",  "x":${batPos.x},  "y":${batPos.y},  "z":${batPos.z}}"""
                                    ),
                                    null
                                )
                        }

                        else -> ChatTransmitter.addToQueue(
                            EnumChatFormatting.RED
                                .toString() + "Dungeon Rooms: Valid options are <chest | wither | superboom | lever | fairysoul | item | entrance | bat | stonk>"
                        )

                    }
                }

                else -> ChatTransmitter.addToQueue(
                    EnumChatFormatting.RED
                        .toString() + "Dungeon Rooms: Run \"/room\" by itself to see the room name or run \"/room help\" for additional options"
                )

            }
        }
    }
}