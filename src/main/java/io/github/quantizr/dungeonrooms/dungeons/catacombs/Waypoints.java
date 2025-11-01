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

package io.github.quantizr.dungeonrooms.dungeons.catacombs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.events.PacketEvent;
import io.github.quantizr.dungeonrooms.utils.MapUtils;
import io.github.quantizr.dungeonrooms.utils.Utils;
import io.github.quantizr.dungeonrooms.utils.WaypointUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Waypoints {
    public static boolean enabled = true;

    public static boolean showEntrance = true;
    public static boolean showSuperboom = true;
    public static boolean showSecrets = true;
    public static boolean showFairySouls = true;
    public static boolean showStonk = true;
    public static boolean sneakToDisable = true;

    public static boolean disableWhenAllFound = true;
    public static boolean allFound = false;

    public static boolean showWaypointText = true;
    public static boolean showBoundingBox = true;
    public static boolean showBeacon = true;

    public static boolean practiceModeOn = false;

    public static int secretNum = 0;
    public static int completedSecrets = 0;

    public static Map<String, List<Boolean>> allSecretsMap = new HashMap<>();
    public static List<Boolean> secretsList = new ArrayList<>(Arrays.asList(new Boolean[10]));

    static long lastSneakTime = 0;
    static long lastCommandTime = 0;
    
    // Store coordinates of found secrets from chat messages
    public static Set<BlockPos> foundSecretCoords = new HashSet<>();

    Frustum frustum = new Frustum();

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!enabled) return;
        if (practiceModeOn && !DungeonRooms.keyBindings[2].isKeyDown()) return;
        String roomName = RoomDetection.roomName;
        if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
        if (DungeonRooms.waypointsJson.get(roomName) != null) {
            JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
            int arraySize = secretsArray.size();
            for(int i = 0; i < arraySize; i++) {
                JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();

                boolean display = true;
                for(int j = 1; j <= secretNum; j++) {
                    if (!secretsList.get(j-1)) {
                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                            display = false;
                            break;
                        }
                    }
                }
                if (!display) continue;

                if (disableWhenAllFound && allFound && !secretsObject.get("category").getAsString().equals("fairysoul")) continue;

                BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);
                
                // Check if this waypoint was marked as found from chat
                if (foundSecretCoords.contains(pos)) {
                    // Mark it as found in the current room
                    for(int j = 1; j <= secretNum; j++) {
                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                            if (Waypoints.secretsList.get(j-1)) {
                                Waypoints.secretsList.set(j-1, false);
                                Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                foundSecretCoords.remove(pos); // Remove from queue once applied
                                
                                // Notify player that queued secret was applied
                                Minecraft.getMinecraft().thePlayer.addChatMessage(
                                    new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §aApplied queued secret #" + j + " from party")
                                );
                                DungeonRooms.logger.info("DungeonRooms: Applied queued secret found for #" + j + " at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                            }
                            break;
                        }
                    }
                    continue; // Don't render this waypoint
                }
                
                Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
                frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
                if (!frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, 255, pos.getZ() + 1)){
                    continue;
                }


                Color color;
                switch (secretsObject.get("category").getAsString()) {
                    case "entrance":
                        if (!showEntrance) continue;
                        color = new Color(0, 255, 0);
                        break;
                    case "superboom":
                        if (!showSuperboom) continue;
                        color = new Color(255, 0, 0);
                        break;
                    case "chest":
                        if (!showSecrets) continue;
                        color = new Color(2, 213, 250);
                        break;
                    case "item":
                        if (!showSecrets) continue;
                        color = new Color(2, 64, 250);
                        break;
                    case "bat":
                        if (!showSecrets) continue;
                        color = new Color(142, 66, 0);
                        break;
                    case "wither":
                        if (!showSecrets) continue;
                        color = new Color(30, 30, 30);
                        break;
                    case "lever":
                        if (!showSecrets) continue;
                        color = new Color(250, 217, 2);
                        break;
                    case "fairysoul":
                        if (!showFairySouls) continue;
                        color = new Color(255, 85, 255);
                        break;
                    case "stonk":
                        if (!showStonk) continue;
                        color = new Color(146, 52, 235);
                        break;
                    default:
                        color = new Color(190, 255, 252);
                }

                double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
                double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
                double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

                double x = pos.getX() - viewerX;
                double y = pos.getY() - viewerY;
                double z = pos.getZ() - viewerZ;
                double distSq = x*x + y*y + z*z;

                GlStateManager.disableDepth();
                GlStateManager.disableCull();
                if (showBoundingBox && frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
                    WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, 0.4f);
                }
                GlStateManager.disableTexture2D();
                if (showBeacon && distSq > 5*5) WaypointUtils.renderBeaconBeam(x, y + 1, z, color.getRGB(), 0.25f, event.partialTicks);
                if (showWaypointText) WaypointUtils.renderWaypointText(secretsObject.get("secretName").getAsString(), pos.up(2), event.partialTicks);
                GlStateManager.disableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.enableCull();
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        
        String message = event.message.getFormattedText();
        
        // Check if dungeon is starting (Mort's message)
        if (message.startsWith("§e[NPC] §bMort§f: §rHere, I found this map when I first entered the dungeon.§r")) {
            // Clear found secrets from previous run
            int clearedCount = foundSecretCoords.size();
            foundSecretCoords.clear();
            if (clearedCount > 0) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §eCleared " + clearedCount + " pending secret(s) from party for new run")
                );
            }
            DungeonRooms.logger.info("DungeonRooms: Cleared found secret coordinates for new dungeon run");
        }
        
        // Action Bar
        if (event.type == 2) {
            String[] actionBarSections = event.message.getUnformattedText().split(" {3,}");

            for (String section : actionBarSections) {
                if (section.contains("Secrets") && section.contains("/")) {
                    String cleanedSection = StringUtils.stripControlCodes(section);
                    String[] splitSecrets = cleanedSection.split("/");

                    completedSecrets = Integer.parseInt(splitSecrets[0].replaceAll("[^0-9]", ""));
                    int totalSecrets = Integer.parseInt(splitSecrets[1].replaceAll("[^0-9]", ""));

                    allFound = (totalSecrets == secretNum && completedSecrets == secretNum);
                    break;
                }
            }
        }
        
        // Regular chat messages (type 0)
        if (event.type == 0) {
            String unformattedMessage = StringUtils.stripControlCodes(event.message.getUnformattedText());
            
            // Check for SS-FOUND pattern with coordinates
            if (unformattedMessage.contains("SS-FOUND-")) {
                // Cancel the event to hide the message from chat
                event.setCanceled(true);
                
                try {
                    // Extract everything after "SS-FOUND-"
                    String coordsStr = unformattedMessage.split("SS-FOUND-")[1].trim();
                    String[] coords = coordsStr.split(",");
                    
                    if (coords.length == 3) {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        int z = Integer.parseInt(coords[2].trim());
                        BlockPos foundPos = new BlockPos(x, y, z);
                        
                        // Add to found secrets
                        boolean isNewSecret = foundSecretCoords.add(foundPos);
                        
                        // Try to disable waypoint immediately if in the right room
                        boolean removed = removeWaypointAtPosition(foundPos);
                        
                        // Only notify if this is a new secret and wasn't immediately removed
                        if (isNewSecret && !removed) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(
                                new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §aQueued secret from party chat (will remove when you enter that room)")
                            );
                        }
                        
                        DungeonRooms.logger.info("DungeonRooms: Received secret found message for coordinates: " + x + "," + y + "," + z);
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
    }
    
    // Helper method to remove waypoint at specific position
    // Returns true if a waypoint was removed, false otherwise
    private boolean removeWaypointAtPosition(BlockPos targetPos) {
        String roomName = RoomDetection.roomName;
        if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return false;
        if (DungeonRooms.waypointsJson.get(roomName) != null) {
            JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
            int arraySize = secretsArray.size();
            for(int i = 0; i < arraySize; i++) {
                JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);
                
                if (pos.equals(targetPos)) {
                    for(int j = 1; j <= secretNum; j++) {
                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                            if (!Waypoints.secretsList.get(j-1)) return false; // Already removed
                            Waypoints.secretsList.set(j-1, false);
                            Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                            
                            // Send feedback to player
                            Minecraft.getMinecraft().thePlayer.addChatMessage(
                                new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §aRemoved secret #" + j + " waypoint from party chat")
                            );
                            DungeonRooms.logger.info("DungeonRooms: Marked secret #" + j + " as found from party chat at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        if (disableWhenAllFound && allFound) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.world.getBlockState(event.pos).getBlock();
            if (block != Blocks.chest && block != Blocks.skull) return;
            String roomName = RoomDetection.roomName;
            if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
            if (DungeonRooms.waypointsJson.get(roomName) != null) {
                JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                int arraySize = secretsArray.size();
                for(int i = 0; i < arraySize; i++) {
                    JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                    if (secretsObject.get("category").getAsString().equals("chest") || secretsObject.get("category").getAsString().equals("wither")) {
                        BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                        BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                        if (pos.equals(event.pos)) {
                            for(int j = 1; j <= secretNum; j++) {
                                if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                    Waypoints.secretsList.set(j-1, false);
                                    Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                    DungeonRooms.logger.info("DungeonRooms: Detected " + secretsObject.get("category").getAsString() + " click, turning off waypoint for secret #" + j);
                                    
                                    // Send command with cooldown check
                                    long currentTime = System.currentTimeMillis();
                                    if (currentTime - lastCommandTime >= 750) {
                                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc SS-FOUND-" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                                        lastCommandTime = currentTime;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        if (disableWhenAllFound && allFound) return;
        Minecraft mc = Minecraft.getMinecraft();

        if (event.packet instanceof S0DPacketCollectItem) {
            S0DPacketCollectItem packet = (S0DPacketCollectItem) event.packet;
            Entity entity = mc.theWorld.getEntityByID(packet.getCollectedItemEntityID());
            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                entity = mc.theWorld.getEntityByID(packet.getEntityID());
                if (entity == null) return;
                String name = item.getEntityItem().getDisplayName();
                if (name.contains("Decoy") || name.contains("Defuse Kit") || name.contains("Dungeon Chest Key") ||
                        name.contains("Healing VIII") || name.contains("Inflatable Jerry") || name.contains("Spirit Leap") ||
                        name.contains("Training Weights") || name.contains("Trap") || name.contains("Treasure Talisman")) {
                    if (!entity.getCommandSenderEntity().getName().equals(mc.thePlayer.getName())) {
                        //Do nothing if someone else picks up the item in order to follow Hypixel rules
                        return;
                    }
                    String roomName = RoomDetection.roomName;
                    if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
                    if (DungeonRooms.waypointsJson.get(roomName) != null) {
                        JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                        int arraySize = secretsArray.size();
                        for(int i = 0; i < arraySize; i++) {
                            JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                            if (secretsObject.get("category").getAsString().equals("item") || secretsObject.get("category").getAsString().equals("bat")) {
                                BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                                if (entity.getDistanceSq(pos) <= 36D) {
                                    for(int j = 1; j <= secretNum; j++) {
                                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                            if (!Waypoints.secretsList.get(j-1)) continue;
                                            Waypoints.secretsList.set(j-1, false);
                                            Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                            DungeonRooms.logger.info("DungeonRooms: " + entity.getCommandSenderEntity().getName() + " picked up " +  StringUtils.stripControlCodes(name) + " from a "  + secretsObject.get("category").getAsString() + " secret, turning off waypoint for secret #" + j);
                                            
                                            // Send command with cooldown check
                                            long currentTime = System.currentTimeMillis();
                                            if (currentTime - lastCommandTime >= 750) {
                                                mc.thePlayer.sendChatMessage("/pc SS-FOUND-" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                                                lastCommandTime = currentTime;
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //Disable waypoint within 4 blocks away on sneak
    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (!Utils.inCatacombs || !enabled || !sneakToDisable) return;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (FMLClientHandler.instance().getClient().gameSettings.keyBindSneak.isPressed()) {
            if (System.currentTimeMillis() - lastSneakTime < 500) { //check for two taps in under half a second
                String roomName = RoomDetection.roomName;
                if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
                if (DungeonRooms.waypointsJson.get(roomName) != null) {
                    JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                    int arraySize = secretsArray.size();
                    for(int i = 0; i < arraySize; i++) {
                        JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                        if (secretsObject.get("category").getAsString().equals("chest") || secretsObject.get("category").getAsString().equals("wither")
                                || secretsObject.get("category").getAsString().equals("item") || secretsObject.get("category").getAsString().equals("bat")) {
                            BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                            BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                            if (player.getDistanceSq(pos) <= 16D) {
                                for(int j = 1; j <= secretNum; j++) {
                                    if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                        if (!Waypoints.secretsList.get(j-1)) continue;
                                        Waypoints.secretsList.set(j-1, false);
                                        Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                        DungeonRooms.logger.info("DungeonRooms: Player sneaked near " + secretsObject.get("category").getAsString() + " secret, turning off waypoint for secret #" + j);
                                        
                                        // Send command with cooldown check
                                        long currentTime = System.currentTimeMillis();
                                        if (currentTime - lastCommandTime >= 750) {
                                            player.sendChatMessage("/pc SS-FOUND-" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                                            lastCommandTime = currentTime;
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            lastSneakTime = System.currentTimeMillis();
        }
    }
}
