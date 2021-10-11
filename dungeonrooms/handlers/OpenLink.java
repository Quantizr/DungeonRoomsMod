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

package io.github.quantizr.dungeonrooms.handlers;

import com.google.gson.JsonObject;
import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.RoomDetection;
import io.github.quantizr.dungeonrooms.gui.LinkGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenLink {

    public static void checkForLink(String type){
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (RoomDetection.roomName.equals("undefined") || DungeonRooms.roomsJson.get(RoomDetection.roomName) == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: You do not appear to be in a detected Dungeon room right now."));
            return;
        }

        JsonObject roomJson = DungeonRooms.roomsJson.get(RoomDetection.roomName).getAsJsonObject();
        if (roomJson.get("dsg").getAsString().equals("null") && roomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no channels/images for this room."));
            return;
        }

        switch (type) {
            case "gui":
                mc.addScheduledTask(() -> mc.displayGuiScreen(new LinkGUI()));
                break;
            case "dsg":
                OpenLink.openDiscord("client");
                break;
            case "sbp":
                if (DungeonRooms.usingSBPSecrets) {
                    OpenLink.openSBPSecrets();
                } else {
                    String sbpURL = "https://discord.gg/2UjaFqfPwJ";
                    ChatComponentText sbp = new ChatComponentText(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.UNDERLINE + sbpURL);
                    sbp.setChatStyle(sbp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sbpURL)));
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: You need the SkyblockPersonalized (SBP) Mod for this feature, get it from ").appendSibling(sbp));
                }
                break;
        }

    }

    public static void openDiscord(String type) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (RoomDetection.roomName.equals("undefined") || DungeonRooms.roomsJson.get(RoomDetection.roomName) == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: You do not appear to be in a detected Dungeon room right now."));
            return;
        }

        JsonObject roomJson = DungeonRooms.roomsJson.get(RoomDetection.roomName).getAsJsonObject();
        if (roomJson.get("dsg").getAsString().equals("null")) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There is no DSG channel for this room."));
            return;
        }
        try {
            if (type.equals("client")){
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Client..."));
                Desktop.getDesktop().browse(new URI("discord://" + roomJson.get("dsg").getAsString()));
            } else {
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Browser..."));
                Desktop.getDesktop().browse(new URI("https://discord.com" + roomJson.get("dsg").getAsString()));
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void openSBPSecrets() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (RoomDetection.roomName.equals("undefined") || DungeonRooms.roomsJson.get(RoomDetection.roomName) == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: You do not appear to be in a detected Dungeon room right now."));
            return;
        }

        JsonObject roomJson = DungeonRooms.roomsJson.get(RoomDetection.roomName).getAsJsonObject();
        if (roomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no SBP images for this room."));
            return;
        }
        String name = roomJson.get("sbp").getAsString();

        String category = roomJson.get("category").getAsString();
        switch (category) {
            case "Puzzle":
            case "Trap":
                category = "puzzles";
                break;
            case "L-shape":
                category = "L";
                break;
        }
        ClientCommandHandler.instance.executeCommand(FMLClientHandler.instance().getClientPlayerEntity(), "/secretoverride " + category + " " + name);
    }
}
