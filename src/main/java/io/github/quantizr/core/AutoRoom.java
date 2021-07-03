/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.core;

import com.google.gson.JsonArray;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AutoRoom {
    Minecraft mc = Minecraft.getMinecraft();

    static int tickAmount = 1;
    public static List<String> autoTextOutput = null;
    public static boolean chatToggled = false;
    public static boolean guiToggled = true;
    public static boolean coordToggled = false;
    public static String lastRoomHash = null;
    public static JsonObject lastRoomJson;
    public static String lastRoomName = null;
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
        if (worldLoad < 200) { //10 seconds
            worldLoad++;
        }

        // Checks every 1.5 seconds
        if (tickAmount % 30 == 0 && Utils.inDungeons && worldLoad == 200) {
            executor.execute(() -> {
                if (AutoRoom.chatToggled || AutoRoom.guiToggled || Waypoints.enabled){
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
        Utils.originBlock = null;
        Utils.originCorner = null;
        worldLoad = 0;
        Waypoints.allSecretsMap.clear();

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
            output.add("Dungeon Rooms: " + EnumChatFormatting.GREEN+ "Press the hotkey \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode()) +"\" to configure");
            output.add(EnumChatFormatting.GREEN + "Secret Waypoints settings.");
            output.add(EnumChatFormatting.WHITE + "(You can change the keybinds in Minecraft controls menu)");
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
        String blockFrequencies = Utils.blockFrequency(x, top, z, true);
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
        //Setting this to true may prevent waypoint flicker, but may cause waypoints to break if Hypixel bugs out
        Waypoints.allFound = false;

        if (DungeonRooms.roomsJson.get(MD5) == null && Utils.getSize(x,top,z).equals("1x1")) {
            output.add(EnumChatFormatting.LIGHT_PURPLE + "Dungeon Rooms: If you see this message in game (and did not create ghost blocks), send a");
            output.add(EnumChatFormatting.LIGHT_PURPLE + "screenshot and the room name to #bug-report channel in the Discord");
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

        JsonArray MD5Array = DungeonRooms.roomsJson.get(MD5).getAsJsonArray();
        int arraySize = MD5Array.size();

        if (arraySize >= 2) {
            boolean floorHashFound = false;
            List<String> chatMessages = new ArrayList<>();

            for(int i = 0; i < arraySize; i++){
                JsonObject roomObject = MD5Array.get(i).getAsJsonObject();
                JsonElement jsonFloorHash = roomObject.get("floorhash");
                if (floorHash != null && jsonFloorHash != null){
                    if (floorHash.equals(jsonFloorHash.getAsString())){
                        String name = roomObject.get("name").getAsString();
                        String category = roomObject.get("category").getAsString();
                        int secrets = roomObject.get("secrets").getAsInt();
                        String fairysoul = "";
                        if (roomObject.get("fairysoul") != null) {
                            fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
                        }
                        output.add(text + category + " - " + name + fairysoul);
                        JsonElement notes = roomObject.get("notes");
                        if (notes != null) {
                            output.add(EnumChatFormatting.GREEN + notes.getAsString());
                        }
                        if (DungeonRooms.waypointsJson.get(name) == null && secrets != 0 && Waypoints.enabled) {
                            output.add(EnumChatFormatting.RED + "No waypoints available");
                            output.add(EnumChatFormatting.RED +  "Press \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) +"\" to view images");
                        }
                        lastRoomJson = roomObject;
                        floorHashFound = true;
                    }
                } else {
                    String name = roomObject.get("name").getAsString();
                    String category = roomObject.get("category").getAsString();
                    int secrets = roomObject.get("secrets").getAsInt();
                    String fairysoul = "";
                    if (roomObject.get("fairysoul") != null) {
                        fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
                    }
                    chatMessages.add(EnumChatFormatting.GREEN + category + " - " + name + fairysoul);
                    JsonElement notes = roomObject.get("notes");
                    if (notes != null) {
                        chatMessages.add(EnumChatFormatting.GREEN + notes.getAsString());
                    }
                    if (DungeonRooms.waypointsJson.get(name) == null && secrets != 0 && Waypoints.enabled) {
                        output.add(EnumChatFormatting.RED + "No waypoints available");
                        output.add(EnumChatFormatting.RED +  "Press \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) +"\" to view images");
                    }
                }
            }
            if (!floorHashFound) {
                output.add("Dungeon Rooms: You are probably in one of the following: ");
                output.add(EnumChatFormatting.AQUA + "(check # of secrets to narrow down rooms)");
                output.addAll(chatMessages);
                output.add(EnumChatFormatting.LIGHT_PURPLE + "Dungeon Rooms: If you see this message in game (and did not create ghost blocks), send a");
                output.add(EnumChatFormatting.LIGHT_PURPLE + "screenshot and the room name to #bug-report channel in the Discord");
                output.add(EnumChatFormatting.AQUA + MD5);
                output.add(EnumChatFormatting.AQUA + floorHash);
                lastRoomJson = null;
            }
        } else {
            JsonObject roomObject = MD5Array.get(0).getAsJsonObject();
            String name = roomObject.get("name").getAsString();
            String category = roomObject.get("category").getAsString();
            int secrets = roomObject.get("secrets").getAsInt();
            String fairysoul = "";
            if (roomObject.get("fairysoul") != null) {
                fairysoul = EnumChatFormatting.WHITE + " - " + EnumChatFormatting.LIGHT_PURPLE + "Fairy Soul";
            }
            output.add(text + category + " - " + name + fairysoul);
            JsonElement notes = roomObject.get("notes");
            if (notes != null) {
                output.add(EnumChatFormatting.GREEN + notes.getAsString());
            }
            if (DungeonRooms.waypointsJson.get(name) == null && secrets != 0 && Waypoints.enabled) {
                output.add(EnumChatFormatting.RED + "No waypoints available");
                output.add(EnumChatFormatting.RED +  "Press \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) +"\" to view images");
            }
            lastRoomJson = roomObject;
        }

        //Store/Retrieve which waypoints to enable
        if (lastRoomJson != null && lastRoomJson.get("name") != null) {
                lastRoomName = lastRoomJson.get("name").getAsString();
                Waypoints.allSecretsMap.putIfAbsent(lastRoomName, new ArrayList<>(Collections.nCopies(9, true)));
                Waypoints.secretsList = Waypoints.allSecretsMap.get(lastRoomName);
        } else {
            lastRoomName = null;
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

    public static void renderCoord() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        BlockPos relativeCoord = Utils.actualToRelative(new BlockPos(player.posX,player.posY,player.posZ));
        if (relativeCoord == null) return;

        List<String> coordDisplay = new ArrayList<>();
        coordDisplay.add("Direction: " + Utils.originCorner);
        coordDisplay.add("Origin: " + Utils.originBlock.getX() + "," + Utils.originBlock.getY() + "," + Utils.originBlock.getZ());
        coordDisplay.add("Relative Pos.: "+ relativeCoord.getX() + "," + relativeCoord.getY() + "," + relativeCoord.getZ());
        int yPos = 0;
        for (String message:coordDisplay) {
            int roomStringWidth = mc.fontRendererObj.getStringWidth(message);
            TextRenderer.drawText(mc, message, ((scaledResolution.getScaledWidth() * 95) / 100) - (roomStringWidth),
                    ((scaledResolution.getScaledHeight() * 5) / 100) + yPos, 1D, true);
            yPos += mc.fontRendererObj.FONT_HEIGHT;
        }
    }
}
