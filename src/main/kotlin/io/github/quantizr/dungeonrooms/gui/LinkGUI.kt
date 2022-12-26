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

import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.handlers.OpenLink
import io.github.quantizr.dungeonrooms.handlers.TextRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

class LinkGUI : GuiScreen() {
    private var discordClient: GuiButton? = null
    private var discordBrowser: GuiButton? = null
    private var SBPSecrets: GuiButton? = null
    private var close: GuiButton? = null
    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun initGui() {
        super.initGui()
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val height = sr.scaledHeight
        val width = sr.scaledWidth
        discordClient = GuiButton(0, width / 2 - 185, height / 6 + 96, 120, 20, "DSG Discord Client")
        discordBrowser = GuiButton(1, width / 2 - 60, height / 6 + 96, 120, 20, "DSG Discord Browser")
        SBPSecrets = GuiButton(2, width / 2 + 65, height / 6 + 96, 120, 20, "SBP Secrets Mod")
        close = GuiButton(3, width / 2 - 60, height / 6 + 136, 120, 20, "Close")
        buttonList.add(discordClient)
        buttonList.add(discordBrowser)
        buttonList.add(SBPSecrets)
        buttonList.add(close)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        val mc = Minecraft.getMinecraft()
        val displayText: String
        displayText = if (DungeonRooms.instance.roomDetection.roomName == "undefined") {
            "Where would you like to view secrets for: ${EnumChatFormatting.RED}undefined"
        } else {
            "Where would you like to view secrets for: ${EnumChatFormatting.GREEN}${DungeonRooms.instance.roomDetection.roomName}"
        }
        val displayWidth = mc.fontRendererObj.getStringWidth(displayText)
        TextRenderer.drawText(mc, displayText, width / 2 - displayWidth / 2, height / 6 + 56, 1.0, false)
        val noteText =
            ("${EnumChatFormatting.GRAY}If you wish to have the hotkey go directly to DSG or SBP instead of this GUI run ${EnumChatFormatting.WHITE}/room set <gui | dsg | sbp>")
        val noteWidth = mc.fontRendererObj.getStringWidth(noteText)
        TextRenderer.drawText(mc, noteText, width / 2 - noteWidth / 2, (height * 0.9).toInt(), 1.0, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    public override fun actionPerformed(button: GuiButton) {
        val player: EntityPlayer = Minecraft.getMinecraft().thePlayer
        if (button === discordClient) {
            OpenLink.openDiscord("client")
            player.closeScreen()
        } else if (button === discordBrowser) {
            OpenLink.openDiscord("browser")
            player.closeScreen()
        } else if (button === SBPSecrets) {
            if (DungeonRooms.usingSBPSecrets) {
                OpenLink.openSBPSecrets()
            } else {
                val sbpURL = "https://discord.gg/2UjaFqfPwJ"
                val sbp =
                    ChatComponentText(EnumChatFormatting.YELLOW.toString() + "" + EnumChatFormatting.UNDERLINE + sbpURL)
                sbp.chatStyle = sbp.chatStyle.setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, sbpURL))
                player.addChatMessage(
                    ChatComponentText(
                        "${EnumChatFormatting.RED}Dungeon Rooms: You need theSkyblock Personalized (SBP) Mod for this feature, get it from "
                    ).appendSibling(sbp)
                )
            }
            player.closeScreen()
        } else if (button === close) {
            player.closeScreen()
        }
    }
}