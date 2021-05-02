/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.quantizr.DungeonRooms;
import io.github.quantizr.handlers.TextRenderer;
import io.github.quantizr.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AutoRoom {
    Minecraft mc = Minecraft.getMinecraft();

    static int tickAmount = 1;
    static List<String> autoTextOutput = null;
    public static boolean chatToggled = false;
    public static boolean guiToggled = true;
    public static String lastRoomHash = null;
    public static JsonObject lastRoomJson;
    private static boolean newRoom = false;
    public static int worldLoad = 0;

    public static int scaleX = 50;
    public static int scaleY = 5;

    private final Executor executor = Executors.newFixedThreadPool(5);

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;

        tickAmount++;
        if (worldLoad < 180) { //9 seconds
            worldLoad++;
        }

        // Checks every 1.5 seconds
        if (tickAmount % 30 == 0 && Utils.inDungeons && worldLoad == 180) {
            executor.execute(() -> {
                if (AutoRoom.chatToggled || AutoRoom.guiToggled){
                    List<String> autoText = autoText();
                    if (autoText != null) {
                        autoTextOutput = autoText;
                    }
                }
                if (AutoRoom.chatToggled) {
                    toggledChat();
                }
            });
            tickAmount = 0;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        Utils.inDungeons = false;
        worldLoad = 0;
        Random random = new Random();
        List<String> output = new ArrayList<>();

        if (random.nextBoolean()) {
            if (DungeonRooms.motd != null) {
                if (!DungeonRooms.motd.isEmpty()) {
                    output.addAll(DungeonRooms.motd);
                }
            }
        }
        if (output.isEmpty()) {
            output.add("Dungeon Rooms: " + EnumChatFormatting.GREEN+ "Press the hotkey \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) +"\" inside a room with secrets");
            output.add(EnumChatFormatting.GREEN + "to view images of the secret locations.");
            output.add(EnumChatFormatting.WHITE + "(You can change the keybind in Minecraft controls menu)");
        }
        autoTextOutput = output;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        Utils.inDungeons = false;
    }

    public static List<String> autoText() {
        List<String> output = new ArrayList<>();
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        int x = (int) Math.floor(player.posX);
        int y = (int) Math.floor(player.posY);
        int z = (int) Math.floor(player.posZ);

        int top = Utils.dungeonTop(x, y, z);
        String blockFrequencies = Utils.blockFrequency(x, top, z);
        if (blockFrequencies == null) return output; //if not in room (under hallway or render distance too low)
        String MD5 = Utils.getMD5(blockFrequencies);
        String floorFrequencies = Utils.floorFrequency(x, top, z);
        String floorHash = Utils.getMD5(floorFrequencies);
        String text = "Dungeon Rooms: You are in " + EnumChatFormatting.GREEN;

        if (MD5.equals("16370f79b2cad049096f881d5294aee6") && !floorHash.equals("94fb12c91c4b46bd0c254edadaa49a3d")) {
            floorHash = "e617eff1d7b77faf0f8dd53ec93a220f"; //exception for box room because floorhash changes when you walk on it
        }

        if (MD5.equals(lastRoomHash) && lastRoomJson != null && floorHash != null) {
            if (lastRoomJson.get("floorhash") != null) {
                if (floorHash.equals(lastRoomJson.get("floorhash").getAsString())) {
                    newRoom = false;
                    return null;
                }
            } else {
                newRoom = false;
                return null;
            }
        }

        newRoom = true;
        lastRoomHash = MD5;

        if (DungeonRooms.roomsJson.get(MD5) == null && Utils.getSize(x,top,z).equals("1x1")) {
            output.add(EnumChatFormatting.LIGHT_PURPLE + "Dungeon Rooms: If you see this message in game, screenshot this and send, along with");
            output.add(EnumChatFormatting.LIGHT_PURPLE + "room name, either directly to _risk#2091 or in the #bug-report channel in the Discord");
            output.add(EnumChatFormatting.LIGHT_PURPLE + "You might get a special role in the Discord and we'll all love you forever");
            output.add(EnumChatFormatting.AQUA + MD5);
            output.add(EnumChatFormatting.AQUA + floorHash);
            output.add("Dungeon Rooms: You are probably in: ");
            output.add(EnumChatFormatting.GREEN + "Literally no idea, all the rooms should have been found");
            lastRoomJson = null;
            return output;
        } else if (DungeonRooms.roomsJson.get(MD5) == null) {
            lastRoomJson = null;
            return output;
        }


        int arraySize = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().size();

        if (arraySize >= 2) {
            boolean floorHashFound = false;
            List<String> chatMessages = new ArrayList<>();

            for(int i = 0; i < arraySize; i++){
                JsonElement jsonFloorHash = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("floorhash");
                if (floorHash != null && jsonFloorHash != null){
                    if (floorHash.equals(jsonFloorHash.getAsString())){
                        String name = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString();
                        String category = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("category").getAsString();
                        String fairysoul = "";
                        if (DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("fairysoul") != null) {
                            fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
                        }
                        output.add(text + category + " - " + name + fairysoul);
                        JsonElement notes = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("notes");
                        if (notes != null) {
                            output.add(EnumChatFormatting.GREEN + notes.getAsString());
                        }
                        lastRoomJson = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject();
                        floorHashFound = true;
                    }
                } else {
                    String name = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString();
                    String category = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("category").getAsString();
                    String fairysoul = "";
                    if (DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("fairysoul") != null) {
                        fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
                    }
                    chatMessages.add(EnumChatFormatting.GREEN + category + " - " + name + fairysoul);
                    JsonElement notes = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("notes");
                    if (notes != null) {
                        chatMessages.add(EnumChatFormatting.GREEN + notes.getAsString());
                    }
                }
            }
            if (!floorHashFound) {
                output.add("Dungeon Rooms: You are probably in one of the following: ");
                output.add(EnumChatFormatting.AQUA + "(check # of secrets to narrow down rooms)");
                output.addAll(chatMessages);
                output.add(EnumChatFormatting.LIGHT_PURPLE + "Dungeon Rooms: If you see this message in game, screenshot this and send, along with");
                output.add(EnumChatFormatting.LIGHT_PURPLE + "the room name, to _risk#2091 or in the #bug-report channel in the Discord");
                output.add(EnumChatFormatting.AQUA + MD5);
                output.add(EnumChatFormatting.AQUA + floorHash);
                lastRoomJson = null;
            }
        } else {
            String name = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
            String category = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject().get("category").getAsString();
            String fairysoul = "";
            if (DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject().get("fairysoul") != null) {
                fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
            }
            output.add(text + category + " - " + name + fairysoul);
            JsonElement notes = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject().get("notes");
            if (notes != null) {
                output.add(EnumChatFormatting.GREEN + notes.getAsString());
            }
            lastRoomJson = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject();
        }
        return output;
    }

    public static void toggledChat() {
        if (!newRoom) return;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (autoTextOutput == null) return;
        if (autoTextOutput.isEmpty()) return;
        for (String message:autoTextOutput) {
            player.addChatMessage(new ChatComponentText(message));
        }
    }

    public static void renderText() {
        if (autoTextOutput == null) return;
        if (autoTextOutput.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int y = 0;
        for (String message:autoTextOutput) {
            int roomStringWidth = mc.fontRendererObj.getStringWidth(message);
            TextRenderer.drawText(mc, message, ((scaledResolution.getScaledWidth() * scaleX) / 100) - (roomStringWidth / 2),
                    ((scaledResolution.getScaledHeight() * scaleY) / 100) + y, 1D, true);
            y += mc.fontRendererObj.FONT_HEIGHT;
        }
    }
}
