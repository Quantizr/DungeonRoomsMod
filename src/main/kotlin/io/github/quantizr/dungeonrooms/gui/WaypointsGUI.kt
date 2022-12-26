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
package io.github.quantizr.dungeonrooms.gui

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.dungeons.Waypoints
import io.github.quantizr.dungeonrooms.handlers.TextRenderer
import io.github.quantizr.dungeonrooms.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import java.io.IOException
import java.util.*
import kotlin.math.ceil

class WaypointsGUI : GuiScreen() {
    private var waypointsEnabled: GuiButton? = null
    private var practiceModeEnabled: GuiButton? = null
    private var showEntrance: GuiButton? = null
    private var showSuperboom: GuiButton? = null
    private var showSecrets: GuiButton? = null
    private var showFairySouls: GuiButton? = null
    private var showStonk: GuiButton? = null
    private var disableWhenAllFound: GuiButton? = null
    private var sneakToDisable: GuiButton? = null
    private var close: GuiButton? = null
    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun initGui() {
        super.initGui()
        waypointGuiOpened = true
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val height = sr.scaledHeight
        val width = sr.scaledWidth
        waypointsEnabled = GuiButton(0, width / 2 - 100 + 0, height / 6 - 5, 200, 20, waypointBtnText())
        practiceModeEnabled = GuiButton(
            1,
            width / 2 - 100 - 110,
            height / 6 + 25,
            200,
            20,
            "Practice Mode: " + getOnOff(DRMConfig.practiceModeOn)
        )
        showEntrance = GuiButton(
            2,
            width / 2 - 100 + 110,
            height / 6 + 25,
            200,
            20,
            "Show Entrance Waypoints: " + getOnOff(DRMConfig.showEntrance)
        )
        showSuperboom = GuiButton(
            3,
            width / 2 - 100 - 110,
            height / 6 + 55,
            200,
            20,
            "Show Superboom Waypoints: " + getOnOff(DRMConfig.showSuperboom)
        )
        showSecrets = GuiButton(
            4,
            width / 2 - 100 + 110,
            height / 6 + 55,
            200,
            20,
            "Show Secret Waypoints: " + getOnOff(DRMConfig.showSecrets)
        )
        showFairySouls = GuiButton(
            5,
            width / 2 - 100 - 110,
            height / 6 + 85,
            200,
            20,
            "Show Fairy Soul Waypoints: " + getOnOff(DRMConfig.showFairySouls)
        )
        showStonk = GuiButton(
            6,
            width / 2 - 100 + 110,
            height / 6 + 85,
            200,
            20,
            "Show Stonk Waypoints: " + getOnOff(DRMConfig.showStonk)
        )
        sneakToDisable = GuiButton(
            7,
            width / 2 - 100 - 110,
            height / 6 + 115,
            200,
            20,
            "Double-Tap Sneak to Hide Nearby: " + getOnOff(DRMConfig.sneakToDisable)
        )
        disableWhenAllFound = GuiButton(
            8,
            width / 2 - 100 + 110,
            height / 6 + 115,
            200,
            20,
            "Disable when all secrets found: " + getOnOff(DRMConfig.disableWhenAllFound)
        )
        close = GuiButton(9, width / 2 - 100, height / 6 * 5, 200, 20, "Close")
        buttonList.add(waypointsEnabled)
        buttonList.add(practiceModeEnabled)
        buttonList.add(showEntrance)
        buttonList.add(showSuperboom)
        buttonList.add(showSecrets)
        buttonList.add(showFairySouls)
        buttonList.add(showStonk)
        buttonList.add(sneakToDisable)
        buttonList.add(disableWhenAllFound)
        buttonList.add(close)
        if (Utils.inCatacombs) {
            if (Waypoints.secretCount > 0) {
                if (Waypoints.secretCount <= 5) {
                    for (i in 1..Waypoints.secretCount) {
                        val adjustPos = -40 * Waypoints.secretCount - 70 + 80 * i
                        secretButtonList[i - 1] = GuiButton(
                            10 + i, width / 2 + adjustPos, height / 6 + 170, 60, 20, "$i: " + getOnOff(
                                Waypoints.secretsList!![i - 1]
                            )
                        )
                        buttonList.add(secretButtonList[i - 1])
                    }
                } else {
                    for (i in 1..ceil(Waypoints.secretCount.toDouble() / 2).toInt()) {
                        val adjustPos = -40 * ceil(Waypoints.secretCount.toDouble() / 2).toInt() - 70 + 80 * i
                        secretButtonList[i - 1] = GuiButton(
                            10 + i, width / 2 + adjustPos, height / 6 + 170, 60, 20, "$i: " + getOnOff(
                                Waypoints.secretsList!![i - 1]
                            )
                        )
                        buttonList.add(secretButtonList[i - 1])
                    }
                    for (i in (ceil((Waypoints.secretCount / 2).toDouble()) + 1).toInt() .. Waypoints.secretCount) {
                        val adjustPos = -40 * (Waypoints.secretCount - ceil(Waypoints.secretCount.toDouble() / 2)
                            .toInt()) - 70 + 80 * (i - ceil(Waypoints.secretCount.toDouble() / 2).toInt())
                        secretButtonList[i - 1] = GuiButton(
                            10 + i, width / 2 + adjustPos, height / 6 + 200, 60, 20, "$i: " + getOnOff(
                                Waypoints.secretsList!![i - 1]
                            )
                        )
                        buttonList.add(secretButtonList[i - 1])
                    }
                }
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        val mc = Minecraft.getMinecraft()
        val text1 = "§lDungeon Room Waypoints:"
        val text1Width = mc.fontRendererObj.getStringWidth(text1)
        TextRenderer.drawText(mc, text1, width / 2 - text1Width / 2, height / 6 - 25, 1.0, false)
        val text2 = "(Use at your own risk)"
        val text2Width = mc.fontRendererObj.getStringWidth(text2)
        TextRenderer.drawText(
            mc,
            EnumChatFormatting.GRAY.toString() + text2,
            width / 2 - text2Width / 2,
            height / 6 - 15,
            1.0,
            false
        )
        val text3 = "Toggle Room Specific Waypoints:"
        val text3Width = mc.fontRendererObj.getStringWidth(text3)
        TextRenderer.drawText(mc, text3, width / 2 - text3Width / 2, height / 6 + 140, 1.0, false)
        val text4 = "(You can also press the # key matching the secret instead)"
        val text4Width = mc.fontRendererObj.getStringWidth(text4)
        TextRenderer.drawText(
            mc,
            EnumChatFormatting.GRAY.toString() + text4,
            width / 2 - text4Width / 2,
            height / 6 + 150,
            1.0,
            false
        )
        if (!Utils.inCatacombs) {
            val errorText = "Not in dungeons"
            val errorTextWidth = mc.fontRendererObj.getStringWidth(errorText)
            TextRenderer.drawText(
                mc,
                EnumChatFormatting.RED.toString() + errorText,
                width / 2 - errorTextWidth / 2,
                height / 6 + 170,
                1.0,
                false
            )
        } else if (Waypoints.secretCount == 0) {
            val errorText = "No secrets in this room"
            val errorTextWidth = mc.fontRendererObj.getStringWidth(errorText)
            TextRenderer.drawText(
                mc,
                EnumChatFormatting.RED.toString() + errorText,
                width / 2 - errorTextWidth / 2,
                height / 6 + 170,
                1.0,
                false
            )
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    public override fun actionPerformed(button: GuiButton) {
        val player: EntityPlayer = Minecraft.getMinecraft().thePlayer
        if (button === waypointsEnabled) {
            DRMConfig.waypointsEnabled = !DRMConfig.waypointsEnabled
            waypointsEnabled!!.displayString = waypointBtnText()
            if (DRMConfig.waypointsEnabled) {
                player.addChatMessage(ChatComponentText("§eDungeon Rooms: Waypoints will now automatically show up when you enter a new dungeon room."))
            }
        } else if (button === practiceModeEnabled) {
            DRMConfig.practiceModeOn = !DRMConfig.practiceModeOn
            practiceModeEnabled!!.displayString = "Practice Mode: " + getOnOff(DRMConfig.practiceModeOn)
            if (DRMConfig.practiceModeOn) {
                player.addChatMessage(
                    ChatComponentText(
                        """
                        §eDungeon Rooms: Practice Mode has been enabled.
                        §e Waypoints will ONLY show up while you are pressing "${
                                DRMConfig.practiceModeKeyBind.display
                        }".
                        §r (Hotkey is configurable in Minecraft Controls menu)
                        """.trimIndent()
                    )
                )
            }
        } else if (button === showEntrance) {
            DRMConfig.showEntrance = !DRMConfig.showEntrance
            showEntrance!!.displayString = "Show Entrance Waypoints: " + getOnOff(DRMConfig.showEntrance)
        } else if (button === showSuperboom) {
            DRMConfig.showSuperboom = !DRMConfig.showSuperboom
            showSuperboom!!.displayString = "Show Superboom Waypoints: " + getOnOff(DRMConfig.showSuperboom)
        } else if (button === showSecrets) {
            DRMConfig.showSecrets = !DRMConfig.showSecrets
            showSecrets!!.displayString = "Show Secret Waypoints: " + getOnOff(DRMConfig.showSecrets)
        } else if (button === showFairySouls) {
            DRMConfig.showFairySouls = !DRMConfig.showFairySouls
            showFairySouls!!.displayString = "Show Fairy Soul Waypoints: " + getOnOff(DRMConfig.showFairySouls)
        } else if (button === showStonk) {
            DRMConfig.showStonk = !DRMConfig.showStonk
            showStonk!!.displayString = "Show Stonk Waypoints: " + getOnOff(DRMConfig.showStonk)
        } else if (button === sneakToDisable) {
            DRMConfig.sneakToDisable = !DRMConfig.sneakToDisable
            sneakToDisable!!.displayString = "Double-Tap Sneak to Hide Nearby: " + getOnOff(DRMConfig.sneakToDisable)
        } else if (button === disableWhenAllFound) {
            DRMConfig.disableWhenAllFound = !DRMConfig.disableWhenAllFound
            disableWhenAllFound!!.displayString =
                "Disable when all secrets found: " + getOnOff(DRMConfig.disableWhenAllFound)
        } else if (button === close) {
            player.closeScreen()
        }
        if (Utils.inCatacombs) {
            if (Waypoints.secretCount > 0) {
                for (i in 1..Waypoints.secretCount) {
                    if (button === secretButtonList[i - 1]) {
                        Waypoints.secretsList!![i - 1] = !Waypoints.secretsList!![i - 1]
                        if (DungeonRooms.instance.roomDetection.roomName != "undefined") {
                            Waypoints.allSecretsMap.replace(DungeonRooms.instance.roomDetection.roomName, Waypoints.secretsList)
                        }
                        secretButtonList[i - 1].displayString = "$i: " + getOnOff(
                            Waypoints.secretsList!![i - 1]
                        )
                        break
                    }
                }
            }
        }
    }

    override fun onGuiClosed() {
        waypointGuiOpened = false
    }

    @Throws(IOException::class)
    override fun keyTyped(c: Char, keyCode: Int) {
        super.keyTyped(c, keyCode)
        if (waypointGuiOpened && Utils.inCatacombs) {
            if (Waypoints.secretCount > 0) {
                for (i in 1..Waypoints.secretCount) {
                    if (keyCode - 1 == i) {
                        Waypoints.secretsList!![i - 1] = !Waypoints.secretsList!![i - 1]
                        if (DungeonRooms.instance.roomDetection.roomName != "undefined") {
                            Waypoints.allSecretsMap.replace(DungeonRooms.instance.roomDetection.roomName, Waypoints.secretsList)
                        }
                        secretButtonList[i - 1].displayString = "$i: " + getOnOff(
                            Waypoints.secretsList!![i - 1]
                        )
                        break
                    }
                }
            }
        }
    }

    companion object {
        var secretButtonList: MutableList<GuiButton> = ArrayList(listOf(*arrayOfNulls(10)))
        private var waypointGuiOpened = false
        private fun waypointBtnText(): String {
            return if (DRMConfig.waypointsEnabled) {
                EnumChatFormatting.GREEN.toString() + "§lWaypoints Enabled"
            } else {
                EnumChatFormatting.RED.toString() + "§lWaypoints Disabled"
            }
        }

        private fun getOnOff(bool: Boolean): String {
            return if (bool) {
                EnumChatFormatting.GREEN.toString() + "On"
            } else {
                EnumChatFormatting.RED.toString() + "Off"
            }
        }
    }
}