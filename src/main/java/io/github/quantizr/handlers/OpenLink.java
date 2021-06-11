/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.handlers;

import io.github.quantizr.DungeonRooms;
import io.github.quantizr.core.AutoRoom;
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
import java.util.List;

public class OpenLink {

    public static void checkForLink(String type){
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (!AutoRoom.chatToggled && !AutoRoom.guiToggled){
            List<String> autoText = AutoRoom.autoText();
            if (autoText != null) {
                AutoRoom.autoTextOutput = autoText;
            }
        }

        if (AutoRoom.lastRoomHash == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: You do not appear to be in a detected Dungeon room right now."));
            return;
        }
        if (AutoRoom.lastRoomJson == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: This command does not work when the current room is detected as one of multiple."));
            return;
        }
        if (AutoRoom.lastRoomJson.get("dsg").getAsString().equals("null") && AutoRoom.lastRoomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no channels/images for this room."));
            return;
        }

        switch (type) {
            case "gui":
                DungeonRooms.guiToOpen = "link";
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
                            + "Dungeon Rooms: You need the Skyblock Personalized (SBP) Mod for this feature, get it from ").appendSibling(sbp));
                }
                break;
        }

    }

    public static void openDiscord(String type) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (AutoRoom.lastRoomJson.get("dsg").getAsString().equals("null")) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There is no DSG channel for this room."));
            return;
        }
        try {
            if (type.equals("client")){
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Client..."));
                Desktop.getDesktop().browse(new URI("discord://" + AutoRoom.lastRoomJson.get("dsg").getAsString()));
            } else {
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Browser..."));
                Desktop.getDesktop().browse(new URI("https://discord.com" + AutoRoom.lastRoomJson.get("dsg").getAsString()));
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void openSBPSecrets() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (AutoRoom.lastRoomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no SBP images for this room."));
            return;
        }
        String name = AutoRoom.lastRoomJson.get("sbp").getAsString();

        String category = AutoRoom.lastRoomJson.get("category").getAsString();
        switch (category) {
            case "Puzzle":
                category = "puzzles";
                break;
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
