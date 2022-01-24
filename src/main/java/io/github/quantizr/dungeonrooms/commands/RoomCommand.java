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

package io.github.quantizr.dungeonrooms.commands;

import com.google.gson.JsonObject;
import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.DungeonManager;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.RoomDetection;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.Waypoints;
import io.github.quantizr.dungeonrooms.gui.WaypointsGUI;
import io.github.quantizr.dungeonrooms.handlers.ConfigHandler;
import io.github.quantizr.dungeonrooms.handlers.OpenLink;
import io.github.quantizr.dungeonrooms.utils.MapUtils;
import io.github.quantizr.dungeonrooms.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoomCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "room";
    }

    @Override
    public String getCommandUsage(ICommandSender arg0) {
        return "/" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("drm");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }


    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "waypoints", "move", "toggle", "set", "discord");
        }
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("toggle")) {
                return getListOfStringsMatchingLastWord(args, "help", "gui", "waypointtext", "waypointboundingbox", "waypointbeacon");
            } else if (args[0].equalsIgnoreCase("set")) {
                return getListOfStringsMatchingLastWord(args, "gui", "dsg", "sbp");
            }
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = (EntityPlayer) arg0;

        if (arg1.length < 1) {
            if (!Utils.inCatacombs) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: Use this command in dungeons or run \"/room help\" for additional options"));
            } else {
                if (DungeonManager.gameStage == 2) {
                    for (String line : DungeonRooms.textToDisplay) {
                        player.addChatMessage(new ChatComponentText(line));
                    }
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN
                            + "Dungeon Rooms: You can also run \"/room help\" for additional options"));
                } else {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: Use this command while clearing rooms or run \"/room help\" for additional options"));
                }
            }
        } else {
            switch (arg1[0].toLowerCase()) {
                case "help":
                    player.addChatMessage(new ChatComponentText("\n"
                            + EnumChatFormatting.GOLD + "Dungeon Rooms Mod Version " + DungeonRooms.VERSION + "\n"
                            + EnumChatFormatting.DARK_PURPLE + "Hotkeys: (Configurable in Controls Menu)\n"
                            + EnumChatFormatting.AQUA + " " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode()) + EnumChatFormatting.WHITE + " - Opens Secret Waypoints configuration GUI\n"
                            + EnumChatFormatting.AQUA + " " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) + EnumChatFormatting.WHITE + " - (old) Opens images of secret locations\n"
                            + EnumChatFormatting.AQUA + " " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[2].getKeyCode()) + EnumChatFormatting.WHITE + " - View waypoints in Practice Mode (\"/room toggle practice\")\n"
                            + EnumChatFormatting.DARK_PURPLE + "Commands:\n"
                            + EnumChatFormatting.AQUA + " /room" + EnumChatFormatting.WHITE + " - Tells you in chat what room you are standing in.\n"
                            + EnumChatFormatting.AQUA + " /room help" + EnumChatFormatting.WHITE + " - Displays this message.\n"
                            + EnumChatFormatting.AQUA + " /room waypoints" + EnumChatFormatting.WHITE + " - Opens Secret Waypoints config GUI, alternatively can be opened with hotkey\n"
                            + EnumChatFormatting.AQUA + " /room move <x> <y>" + EnumChatFormatting.WHITE + " - Moves the GUI room name text to a coordinate. <x> and <y> are numbers between 0 and 100. Default is 50 for <x> and 5 for <y>.\n"
                            + EnumChatFormatting.AQUA + " /room toggle [argument]" + EnumChatFormatting.WHITE + " - Run \"/room toggle help\" for full list of toggles.\n"
                            + EnumChatFormatting.AQUA + " /room set <gui | dsg | sbp>" + EnumChatFormatting.WHITE + " - Configure whether the hotkey opens the selector GUI or directly goes to DSG/SBP.\n"
                            + EnumChatFormatting.AQUA + " /room discord" + EnumChatFormatting.WHITE + " - Opens the Discord invite for this mod in your browser.\n" /* +
                            + EnumChatFormatting.AQUA + " /room open" + EnumChatFormatting.WHITE + " - Opens the gui for opening either DSG or SBP.\n"
                            + EnumChatFormatting.AQUA + " /room dsg" + EnumChatFormatting.WHITE + " - Directly opens DSG in the Discord client.\n"
                            + EnumChatFormatting.AQUA + " /room sbp" + EnumChatFormatting.WHITE + " - Directly opens the SBP secrets (if you have the mod installed).\n" */
                    ));
                    break;

                case "gui":
                case "open":
                    if (!Utils.inCatacombs) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Use this command in dungeons"));
                        return;
                    }
                    OpenLink.checkForLink("gui");
                    break;

                case "dsg":
                    if (!Utils.inCatacombs) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Use this command in dungeons"));
                        return;
                    }
                    OpenLink.checkForLink("dsg");
                    break;

                case "sbp":
                    if (!Utils.inCatacombs) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Use this command in dungeons"));
                        return;
                    }
                    OpenLink.checkForLink("sbp");
                    break;

                case "set":
                    switch (arg1[1].toLowerCase()) {
                        case "gui":
                            DungeonRooms.imageHotkeyOpen = "gui";
                            player.addChatMessage(new ChatComponentText("Dungeon Rooms: Hotkey has been set to open: GUI"));
                            ConfigHandler.writeStringConfig("gui", "hotkeyOpen", "gui");
                            break;
                        case "dsg":
                            DungeonRooms.imageHotkeyOpen = "dsg";
                            player.addChatMessage(new ChatComponentText("Dungeon Rooms: Hotkey has been set to open: DSG"));
                            ConfigHandler.writeStringConfig("gui", "hotkeyOpen", "dsg");
                            break;
                        case "sbp":
                            DungeonRooms.imageHotkeyOpen = "sbp";
                            player.addChatMessage(new ChatComponentText("Dungeon Rooms: Hotkey has been set to open: SBP"));
                            ConfigHandler.writeStringConfig("gui", "hotkeyOpen", "sbp");
                            break;
                        default:
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Valid options are <gui | dsg | sbp>"));
                            break;
                    }
                    break;

                case "wp":
                case "waypoint":
                case "waypoints":
                    new Thread(() -> { mc.addScheduledTask(() -> mc.displayGuiScreen(new WaypointsGUI())); }).start();
                    break;

                case "move":
                    DungeonRooms.textLocX = Integer.parseInt(arg1[1]);
                    DungeonRooms.textLocY = Integer.parseInt(arg1[2]);
                    ConfigHandler.writeIntConfig("gui", "scaleX", DungeonRooms.textLocX);
                    ConfigHandler.writeIntConfig("gui", "scaleY", DungeonRooms.textLocY);
                    player.addChatMessage(new ChatComponentText("Dungeon Rooms: Room GUI has been moved to " + arg1[1] + ", " + arg1[2]));
                    break;

                case "toggle":
                    String toggleHelp = "\n"
                            + EnumChatFormatting.GOLD + " Dungeon Rooms Mod Toggle Commands:" + "\n"
                            + EnumChatFormatting.AQUA + " /room toggle gui" + EnumChatFormatting.WHITE + " - Toggles displaying current room in gui.\n"
                            + EnumChatFormatting.AQUA + " /room toggle motd" + EnumChatFormatting.WHITE + " - Toggles displaying the Welcome/MOTD message at the start of a dungeon run.\n"
                            + EnumChatFormatting.AQUA + " /room toggle practice" + EnumChatFormatting.WHITE + " - Toggles Practice Mode, where waypoints are only displayed when holding down " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[2].getKeyCode()) +"\".\n"
                            + EnumChatFormatting.AQUA + " /room toggle waypoints" + EnumChatFormatting.WHITE + " - Toggles Waypoints, does the same thing as pressing \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode()) +"\" then clicking \"Waypoints\".\n"
                            + EnumChatFormatting.AQUA + " /room toggle waypointtext" + EnumChatFormatting.WHITE + " - Toggles displaying waypoint names above waypoints.\n"
                            + EnumChatFormatting.AQUA + " /room toggle waypointboundingbox" + EnumChatFormatting.WHITE + " - Toggles displaying the bounding box on waypoints.\n"
                            + EnumChatFormatting.AQUA + " /room toggle waypointbeacon" + EnumChatFormatting.WHITE + " - Toggles displaying the beacon above waypoints.\n";

                    if (arg1.length == 1) {
                        player.addChatMessage(new ChatComponentText(toggleHelp));
                        break;
                    } else {
                        switch (arg1[1].toLowerCase()) {
                            case "gui":
                                DungeonManager.guiToggled = !DungeonManager.guiToggled;
                                ConfigHandler.writeBooleanConfig("toggles", "guiToggled", DungeonManager.guiToggled);
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Display room names in GUI has been set to: " + DungeonManager.guiToggled));
                                break;

                            case "welcome":
                            case "motd":
                                DungeonManager.motdToggled = !DungeonManager.motdToggled;
                                ConfigHandler.writeBooleanConfig("toggles", "motdToggled", DungeonManager.motdToggled);
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Display Welcome/MOTD has been set to: " + DungeonManager.motdToggled));
                                break;

                            case "practice":
                            case "practicemode":
                                Waypoints.practiceModeOn = !Waypoints.practiceModeOn;
                                ConfigHandler.writeBooleanConfig("waypoint", "practiceModeOn", Waypoints.practiceModeOn);
                                if (Waypoints.practiceModeOn) {
                                    player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Practice Mode has been enabled.\n"
                                            + "§e Waypoints will only show up while you are pressing \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[2].getKeyCode()) + "\".\n"
                                            + "§r (Hotkey is configurable in Minecraft Controls menu)"
                                    ));
                                } else {
                                    player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Practice Mode has been disabled."));
                                }
                                break;

                            case "waypoint":
                            case "waypoints":
                                Waypoints.enabled = !Waypoints.enabled;
                                ConfigHandler.writeBooleanConfig("waypoint", "waypointsToggled", Waypoints.enabled);
                                if (Waypoints.enabled) {
                                    player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Waypoints will now automatically show up when you enter a new dungeon room."));
                                } else {
                                    player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Waypoints have been disabled."));
                                }
                                break;

                            case "text":
                            case "waypointtext":
                                Waypoints.showWaypointText = !Waypoints.showWaypointText;
                                ConfigHandler.writeBooleanConfig("waypoint", "showWaypointText", Waypoints.showWaypointText);
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Show Waypoint Text has been set to: " + Waypoints.showWaypointText));
                                break;

                            case "boundingbox":
                            case "waypointboundingbox":
                                Waypoints.showBoundingBox = !Waypoints.showBoundingBox;
                                ConfigHandler.writeBooleanConfig("waypoint", "showBoundingBox", Waypoints.showBoundingBox);
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Show Waypoint Bounding Box has been set to: " + Waypoints.showBoundingBox));
                                break;

                            case "beacon":
                            case "waypointbeacon":
                                Waypoints.showBeacon = !Waypoints.showBeacon;
                                ConfigHandler.writeBooleanConfig("waypoint", "showBeacon", Waypoints.showBeacon);
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Show Waypoint Beacon has been set to: " + Waypoints.showBeacon));
                                break;

                            /*
                            case "dev":
                            case "coord":
                                AutoRoom.coordToggled = !AutoRoom.coordToggled;
                                ConfigHandler.writeBooleanConfig("toggles", "coordToggled", AutoRoom.coordToggled);
                                player.addChatMessage(new ChatComponentText("Display dev coords has been set to: " + AutoRoom.coordToggled));
                                break;
                            */

                            case "override":
                                Utils.dungeonOverride = !Utils.dungeonOverride;
                                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Force inCatacombs has been set to: " + Utils.dungeonOverride));
                                break;

                            case "help":
                            default:
                                player.addChatMessage(new ChatComponentText(toggleHelp));
                                break;
                        }
                    }
                    break;

                case "reload":
                    ConfigHandler.reloadConfig();
                    player.addChatMessage(new ChatComponentText("Dungeon Rooms: Reloaded config file"));
                    break;

                case "discord":
                    try {
                        player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening Dungeon Rooms Discord invite in browser..."));
                        Desktop.getDesktop().browse(new URI("https://discord.gg/7B5RbsArYK"));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                    break;

                case "relative":
                    if (!RoomDetection.roomDirection.equals("undefined") && RoomDetection.roomCorner != null) {
                        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                            BlockPos relativePos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos(), RoomDetection.roomDirection, RoomDetection.roomCorner);
                            player.addChatMessage(new ChatComponentText("Dungeon Rooms: You are looking at relative blockPos: " + relativePos));
                        }
                    } else {
                        player.addChatMessage(new ChatComponentText("Dungeon Rooms: Unable to get relative blockPos at this time."));
                    }
                    break;

                case "json":
                    if (!Utils.inCatacombs && DungeonManager.gameStage != 2 && DungeonManager.gameStage != 3) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Use this command in dungeons"));
                        return;
                    }
                    if (DungeonRooms.roomsJson.get(RoomDetection.roomName) != null) {
                        JsonObject json = DungeonRooms.roomsJson.get(RoomDetection.roomName).getAsJsonObject();
                        json.addProperty("name", RoomDetection.roomName); //add room name property
                        player.addChatMessage(new ChatComponentText(json.toString()));
                    }
                    break;

                //return available information about a room
                case "roominfo":
                    if (!Utils.inCatacombs || DungeonManager.gameStage != 2) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Not in room clearing phase of dungeons"));
                    }
                    if (DungeonManager.entranceMapCorners != null) {
                        player.addChatMessage(new ChatComponentText("dev: entranceMapCorners = " + new ArrayList<>(Arrays.asList(DungeonManager.entranceMapCorners))));
                    } else {
                        player.addChatMessage(new ChatComponentText("dev: entranceMapCorners = null"));
                    }
                    if (DungeonManager.entrancePhysicalNWCorner != null) {
                        player.addChatMessage(new ChatComponentText("dev: entrancePhysicalNWCorner = " + DungeonManager.entrancePhysicalNWCorner));
                    } else {
                        player.addChatMessage(new ChatComponentText("dev: entrancePhysicalNWCorner = null"));
                    }
                    player.addChatMessage(new ChatComponentText("dev: currentMapSegments = " + RoomDetection.currentMapSegments));
                    player.addChatMessage(new ChatComponentText("dev: currentPhysicalSegments = " + RoomDetection.currentPhysicalSegments));
                    player.addChatMessage(new ChatComponentText("dev: roomName = " + RoomDetection.roomName));
                    player.addChatMessage(new ChatComponentText("dev: roomSize = " + RoomDetection.roomSize));
                    player.addChatMessage(new ChatComponentText("dev: roomColor = " + RoomDetection.roomColor));
                    player.addChatMessage(new ChatComponentText("dev: roomCategory = " + RoomDetection.roomCategory));

                    player.addChatMessage(new ChatComponentText("dev: roomDirection = " + RoomDetection.roomDirection));
                    if (RoomDetection.roomCorner != null) {
                        player.addChatMessage(new ChatComponentText("dev: roomCorner = " + RoomDetection.roomCorner));
                    } else {
                        player.addChatMessage(new ChatComponentText("dev: roomCorner = null"));
                    }
                    break;

                //returns the list of BlockPos which were checked against ".skeleton" room data files before room was determined
                case "blocksused":
                    if (!Utils.inCatacombs || DungeonManager.gameStage != 2) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Not in room clearing phase of dungeons"));
                    }
                    player.addChatMessage(new ChatComponentText(RoomDetection.blocksUsed.toString()));
                    break;

                //The following is for adding new rooms
                case "add":
                    World world = mc.theWorld;
                    if (!Utils.inCatacombs || DungeonManager.gameStage != 2 || RoomDetection.roomDirection.equals("undefined") || RoomDetection.roomCorner == null) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Current dungeon room is undefined"));
                        return;
                    }
                    switch (arg1[1].toLowerCase()) {
                        case "chest":
                            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                BlockPos viewingPos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos(), RoomDetection.roomDirection, RoomDetection.roomCorner);
                                if (world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.chest) {
                                    player.addChatMessage(new ChatComponentText("{\n" +
                                            "  \"secretName\":\"# - Chest\",\n" +
                                            "  \"category\":\"chest\",\n" +
                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                            "}"));
                                    Toolkit.getDefaultToolkit()
                                            .getSystemClipboard()
                                            .setContents(
                                                    new StringSelection("{\n" +
                                                            "  \"secretName\":\"# - Chest\",\n" +
                                                            "  \"category\":\"chest\",\n" +
                                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                                            "}"),
                                                    null
                                            );
                                } else {
                                    player.addChatMessage(new ChatComponentText("You are not looking at a Chest Secret"));
                                }
                            } else {
                                player.addChatMessage(new ChatComponentText("You are not looking at anything"));
                            }
                            break;
                        case "wither":
                            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                BlockPos viewingPos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos(), RoomDetection.roomDirection, RoomDetection.roomCorner);
                                if (world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.skull) {
                                    player.addChatMessage(new ChatComponentText("{\n" +
                                            "  \"secretName\":\"# - Wither Essence\",\n" +
                                            "  \"category\":\"wither\",\n" +
                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                            "}"));
                                    Toolkit.getDefaultToolkit()
                                            .getSystemClipboard()
                                            .setContents(
                                                    new StringSelection("{\n" +
                                                            "  \"secretName\":\"# - Wither Essence\",\n" +
                                                            "  \"category\":\"wither\",\n" +
                                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                                            "}"),
                                                    null
                                            );
                                } else {
                                    player.addChatMessage(new ChatComponentText("You are not looking at a Wither Essence Secret"));
                                }
                            } else {
                                player.addChatMessage(new ChatComponentText("You are not looking at anything"));
                            }
                            break;
                        case "superboom":
                            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                BlockPos viewingPos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos(), RoomDetection.roomDirection, RoomDetection.roomCorner);
                                if (world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.stonebrick) {
                                    player.addChatMessage(new ChatComponentText("{\n" +
                                            "  \"secretName\":\"# - Superboom\",\n" +
                                            "  \"category\":\"superboom\",\n" +
                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                            "}"));
                                    Toolkit.getDefaultToolkit()
                                            .getSystemClipboard()
                                            .setContents(
                                                    new StringSelection("{\n" +
                                                            "  \"secretName\":\"# - Superboom\",\n" +
                                                            "  \"category\":\"superboom\",\n" +
                                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                                            "}"),
                                                    null
                                            );
                                } else {
                                    player.addChatMessage(new ChatComponentText("You are not looking at a Superboom entrance"));
                                }
                            } else {
                                player.addChatMessage(new ChatComponentText("You are not looking at anything"));
                            }
                            break;
                        case "lever":
                            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                BlockPos viewingPos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos(), RoomDetection.roomDirection, RoomDetection.roomCorner);
                                if (world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.lever) {
                                    player.addChatMessage(new ChatComponentText("{\n" +
                                            "  \"secretName\":\"# - Lever\",\n" +
                                            "  \"category\":\"lever\",\n" +
                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                            "}"));
                                    Toolkit.getDefaultToolkit()
                                            .getSystemClipboard()
                                            .setContents(
                                                    new StringSelection("{\n" +
                                                            "  \"secretName\":\"# - Lever\",\n" +
                                                            "  \"category\":\"lever\",\n" +
                                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                                            "}"),
                                                    null
                                            );
                                } else {
                                    player.addChatMessage(new ChatComponentText("You are not looking at a Lever"));
                                }
                            } else {
                                player.addChatMessage(new ChatComponentText("You are not looking at anything"));
                            }
                            break;
                        case "fairysoul":
                            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                BlockPos viewingPos = MapUtils.actualToRelative(mc.objectMouseOver.getBlockPos().up(1), RoomDetection.roomDirection, RoomDetection.roomCorner);
                                if (world.getBlockState(mc.objectMouseOver.getBlockPos().up(1)).getBlock() == Blocks.air) {
                                    player.addChatMessage(new ChatComponentText("{\n" +
                                            "  \"secretName\":\"Fairy Soul\",\n" +
                                            "  \"category\":\"fairysoul\",\n" +
                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                            "}"));
                                    Toolkit.getDefaultToolkit()
                                            .getSystemClipboard()
                                            .setContents(
                                                    new StringSelection("{\n" +
                                                            "  \"secretName\":\"Fairy Soul\",\n" +
                                                            "  \"category\":\"fairysoul\",\n" +
                                                            "  \"x\":" + viewingPos.getX() + ",\n" +
                                                            "  \"y\":" + viewingPos.getY() + ",\n" +
                                                            "  \"z\":" + viewingPos.getZ() + "\n" +
                                                            "}"),
                                                    null
                                            );
                                } else {
                                    player.addChatMessage(new ChatComponentText("You are not looking at the block below a Fairy Soul"));
                                }
                            } else {
                                player.addChatMessage(new ChatComponentText("You are not looking at anything"));
                            }
                            break;
                        case "item":
                            BlockPos playerPos = MapUtils.actualToRelative(new BlockPos(player.posX,player.posY,player.posZ), RoomDetection.roomDirection, RoomDetection.roomCorner);
                            player.addChatMessage(new ChatComponentText("{\n" +
                                    "  \"secretName\":\"# - Item\",\n" +
                                    "  \"category\":\"item\",\n" +
                                    "  \"x\":" + playerPos.getX() + ",\n" +
                                    "  \"y\":" + playerPos.getY() + ",\n" +
                                    "  \"z\":" + playerPos.getZ() + "\n" +
                                    "}"));
                            Toolkit.getDefaultToolkit()
                                    .getSystemClipboard()
                                    .setContents(
                                            new StringSelection("{\n" +
                                                    "  \"secretName\":\"# - Item\",\n" +
                                                    "  \"category\":\"item\",\n" +
                                                    "  \"x\":" + playerPos.getX() + ",\n" +
                                                    "  \"y\":" + playerPos.getY() + ",\n" +
                                                    "  \"z\":" + playerPos.getZ() + "\n" +
                                                    "}"),
                                            null
                                    );
                            break;
                        case "entrance":
                            BlockPos entrancePos = MapUtils.actualToRelative(new BlockPos(player.posX,player.posY + 1, player.posZ), RoomDetection.roomDirection, RoomDetection.roomCorner);
                            player.addChatMessage(new ChatComponentText("{\n" +
                                    "  \"secretName\":\"# - Entrance\",\n" +
                                    "  \"category\":\"entrance\",\n" +
                                    "  \"x\":" + entrancePos.getX() + ",\n" +
                                    "  \"y\":" + entrancePos.getY() + ",\n" +
                                    "  \"z\":" + entrancePos.getZ() + "\n" +
                                    "}"));
                            Toolkit.getDefaultToolkit()
                                    .getSystemClipboard()
                                    .setContents(
                                            new StringSelection("{\n" +
                                                    "  \"secretName\":\"# - Entrance\",\n" +
                                                    "  \"category\":\"entrance\",\n" +
                                                    "  \"x\":" + entrancePos.getX() + ",\n" +
                                                    "  \"y\":" + entrancePos.getY() + ",\n" +
                                                    "  \"z\":" + entrancePos.getZ() + "\n" +
                                                    "}"),
                                            null
                                    );
                            break;
                        case "bat":
                            BlockPos batPos = MapUtils.actualToRelative(new BlockPos(player.posX,player.posY + 1, player.posZ), RoomDetection.roomDirection, RoomDetection.roomCorner);
                            player.addChatMessage(new ChatComponentText("{\n" +
                                    "  \"secretName\":\"# - Bat\",\n" +
                                    "  \"category\":\"bat\",\n" +
                                    "  \"x\":" + batPos.getX() + ",\n" +
                                    "  \"y\":" + batPos.getY() + ",\n" +
                                    "  \"z\":" + batPos.getZ() + "\n" +
                                    "}"));
                            Toolkit.getDefaultToolkit()
                                    .getSystemClipboard()
                                    .setContents(
                                            new StringSelection("{\n" +
                                                    "  \"secretName\":\"# - Bat\",\n" +
                                                    "  \"category\":\"bat\",\n" +
                                                    "  \"x\":" + batPos.getX() + ",\n" +
                                                    "  \"y\":" + batPos.getY() + ",\n" +
                                                    "  \"z\":" + batPos.getZ() + "\n" +
                                                    "}"),
                                            null
                                    );
                            break;
                        default:
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Valid options are <chest | wither | superboom | lever | fairysoul | item | entrance | bat | stonk>"));
                            break;
                    }
                    break;

                default:
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: Run \"/room\" by itself to see the room name or run \"/room help\" for additional options"));
            }
        }
    }
}
