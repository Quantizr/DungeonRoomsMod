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

import io.github.quantizr.dungeonrooms.ChatTransmitter
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.gui.LinkGUI
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.client.FMLClientHandler
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

object OpenLink {
    @JvmStatic
    fun checkForLink(type: String?) {
        val mc = Minecraft.getMinecraft()

        (DungeonRooms.instance.getSecretsObject() ?: return).let { (obj, _) ->
            if (obj["dsg"].asString == "null" && obj["sbp"] == null) {
                ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: There are no channels/images for this room.")
                return
            }
        }

        when (type) {
            "gui" -> mc.addScheduledTask { mc.displayGuiScreen(LinkGUI()) }
            "dsg" -> openDiscord("client")
            "sbp" -> if (DungeonRooms.usingSBPSecrets) {
                openSBPSecrets()
            } else {
                val sbpURL = "https://discord.gg/2UjaFqfPwJ"
                val sbp = ChatComponentText("${EnumChatFormatting.YELLOW}${EnumChatFormatting.UNDERLINE}$sbpURL")
                sbp.chatStyle = sbp.chatStyle.setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, sbpURL))
                val thacomponent = ChatComponentText(
                    "${EnumChatFormatting.RED}Dungeon Rooms: You need the SkyblockPersonalized (SBP) Mod for this feature, get it from "
                ).appendSibling(sbp)
                ChatTransmitter.addToQueue(thacomponent)
            }
        }
    }

    fun openDiscord(type: String) {
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer

        val (roomJson, _) = DungeonRooms.instance.getSecretsObject() ?: return

        if (roomJson["dsg"].asString == "null") {
            ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: There is no DSG channel for this room.")
            return
        }
        try {
            if (type == "client") {
                player.addChatMessage(ChatComponentText("Dungeon Rooms: Opening DSG Discord in Client..."))
                Desktop.getDesktop().browse(URI("discord://" + roomJson["dsg"].asString))
            } else {
                player.addChatMessage(ChatComponentText("Dungeon Rooms: Opening DSG Discord in Browser..."))
                Desktop.getDesktop().browse(URI("https://discord.com" + roomJson["dsg"].asString))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun openSBPSecrets() {
        val (roomJson, _) = DungeonRooms.instance.getSecretsObject() ?: return
        if (roomJson["sbp"] == null) {
            ChatTransmitter.addToQueue("${EnumChatFormatting.RED}Dungeon Rooms: There are no SBP images for this room.")
            return
        }
        val name = roomJson["sbp"].asString
        var category = roomJson["category"].asString
        when (category) {
            "Puzzle", "Trap" -> category = "puzzles"
            "L-shape" -> category = "L"
        }
        ClientCommandHandler.instance.executeCommand(
            FMLClientHandler.instance().clientPlayerEntity,
            "/secretoverride $category $name"
        )
    }
}