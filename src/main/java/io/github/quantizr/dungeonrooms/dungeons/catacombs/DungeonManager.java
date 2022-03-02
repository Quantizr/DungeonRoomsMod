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

import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.utils.MapUtils;
import io.github.quantizr.dungeonrooms.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DungeonManager {
    Minecraft mc = Minecraft.getMinecraft();
    public static int gameStage = 0; //0: Not in Dungeon, 1: Entrance/Not Started, 2: Room Clear, 3: Boss, 4: Done

    public static boolean guiToggled = true;
    public static boolean motdToggled = true;

    public static Integer[][] map;
    public static Point[] entranceMapCorners;
    public static Point entrancePhysicalNWCorner;

    public static int tickAmount = 0;

    long bloodTime = Long.MAX_VALUE;

    boolean oddRun = true; //if current run number is even or odd

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inCatacombs) return;
        String message = event.message.getFormattedText();

        //gameStage set from 0 to 1 in the onTick function later
        if (message.startsWith("§e[NPC] §bMort§f: §rHere, I found this map when I first entered the dungeon.§r")) {
            gameStage = 2;
            DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);
        } else if (message.startsWith("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodTime = System.currentTimeMillis() + 5000; //5 seconds because additional messages might come through
            DungeonRooms.logger.info("DungeonRooms: bloodDone has been set to True");
        } else if (System.currentTimeMillis() > bloodTime && ((message.startsWith("§r§c[BOSS] ") && !message.contains(" The Watcher§r§f:")) || message.startsWith("§r§4[BOSS] "))) {
            if (gameStage != 3) {
                gameStage = 3;
                DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);

                //this part mostly so /room json doesn't error out
                RoomDetection.resetCurrentRoom();
                RoomDetection.roomName = "Boss Room";
                RoomDetection.roomCategory = "General";
                //RoomDetection.newRoom() //uncomment to display Boss Room in gui
            }
        } else if (message.contains("§r§c☠ §r§eDefeated §r")) {
            gameStage = 4;
            DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);
            RoomDetection.resetCurrentRoom();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        EntityPlayerSP player = mc.thePlayer;

        if (!Utils.inCatacombs) return; //From this point forward, everything assumes that Utils.inCatacombs == true
        tickAmount++;

        if ((gameStage == 0 || gameStage == 1) && tickAmount % 20 == 0) {

            if (DungeonRooms.firstLogin) {
                DungeonRooms.firstLogin = false;
                mc.thePlayer.addChatMessage(new ChatComponentText("§d§l--- Dungeon Rooms Mod ---\n"
                        + "§e This appears to be your first time using DRM v" + DungeonRooms.VERSION + ".\n"
                        + "§e Press \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode())
                        +"\" to configure Secret Waypoint settings, If you do not wish to use Waypoints, you can instead press \""
                        + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode())
                        +"\" while inside a dungeon room to view images of the secrets for that room.\n"
                        + "§r (If you need help, join the Discord! Run \"/room discord\" to open the Discord invite.)\n"
                        + "§d§l------------------------"
                ));
            }

            if (gameStage == 0) {
                Utils.checkForConflictingHotkeys();
                gameStage = 1;
                DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);
            }

            Integer[][] map = MapUtils.updatedMap();
            if (map != null) {
                DungeonRooms.logger.warn("DungeonRooms: Run started but gameStage is not on 2");
                gameStage = 2;
                DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);
                return;
            }

            if (gameStage == 1 && entrancePhysicalNWCorner == null) {
                if (!player.getPositionVector().equals(new Vec3(0.0D,0.0D,0.0D))) {
                    //this point is calculated using math, not scanning, which may cause issues when reconnecting to a run
                    entrancePhysicalNWCorner = MapUtils.getClosestNWPhysicalCorner(player.getPositionVector());
                    DungeonRooms.logger.info("DungeonRooms: entrancePhysicalNWCorner has been set to " + entrancePhysicalNWCorner);
                }
            }

            if (DungeonRooms.textToDisplay == null && motdToggled) {
                DungeonRooms.logger.info("DungeonRooms: Updating MOTD on screen");
                if (oddRun || !guiToggled) { //load MOTD on odd runs
                    if (DungeonRooms.motd != null) {
                        if (!DungeonRooms.motd.isEmpty()) {
                            DungeonRooms.textToDisplay = DungeonRooms.motd;
                        }
                    }
                }
                if (DungeonRooms.textToDisplay == null && guiToggled) { //if MOTD is empty or not odd run load default text
                    DungeonRooms.textToDisplay = new ArrayList<>(Arrays.asList(
                            "Dungeon Rooms: " + EnumChatFormatting.GREEN + "Press the hotkey \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode()) +"\" to configure",
                            EnumChatFormatting.GREEN + " waypoint settings. Alternatively, press \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) + "\" while in a room",
                            EnumChatFormatting.GREEN + "to view images of secret locations for that room.",
                            "(You can change the keybinds in Minecraft controls menu)"
                    ));
                }
                oddRun = !oddRun;
            }

            tickAmount = 0;
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        Utils.inCatacombs = false;
        tickAmount = 0;
        gameStage = 0;

        map = null;
        entranceMapCorners = null;
        entrancePhysicalNWCorner = null;
        RoomDetection.entranceMapNullCount = 0;

        bloodTime = Long.MAX_VALUE;

        if (RoomDetection.stage2Executor != null) RoomDetection.stage2Executor.shutdown();

        Waypoints.allSecretsMap.clear();

        RoomDetection.resetCurrentRoom();
    }
}
