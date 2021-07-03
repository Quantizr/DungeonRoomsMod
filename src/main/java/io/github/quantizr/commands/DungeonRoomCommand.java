/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.commands;

import com.google.gson.JsonElement;
import io.github.quantizr.DungeonRooms;
import io.github.quantizr.core.AutoRoom;
import io.github.quantizr.core.Waypoints;
import io.github.quantizr.handlers.ConfigHandler;
import io.github.quantizr.handlers.OpenLink;
import io.github.quantizr.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class DungeonRoomCommand extends CommandBase {

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
        return Collections.singletonList("dungeonroom");
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
                return getListOfStringsMatchingLastWord(args, "help", "gui", "chat", "waypointtext", "waypointboundingbox", "waypointbeacon");
            } else if (args[0].equalsIgnoreCase("set")) {
                return getListOfStringsMatchingLastWord(args, "gui", "dsg", "sbp");
            }
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) {
        new Thread(() -> {
            EntityPlayer player = (EntityPlayer) arg0;

            int x = (int) Math.floor(player.posX);
            int y = (int) Math.floor(player.posY);
            int z = (int) Math.floor(player.posZ);

            if (arg1.length < 1) {
                if (!Utils.inDungeons) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: Use this command in dungeons or run \"/room help\" for additional options"));
                    return;
                }
                int top = Utils.dungeonTop(x,y,z);
                String blockFrequencies = Utils.blockFrequency(x,top,z,true);
                if (blockFrequencies == null) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: Make sure you aren't in a hallway between rooms and that your render distance is high enough."));
                    return;
                }
                List<String> autoText = AutoRoom.autoText();
                if (autoText != null) {
                    AutoRoom.autoTextOutput = autoText;
                }
                if (AutoRoom.autoTextOutput == null) return;
                if (AutoRoom.autoTextOutput.isEmpty()) return;
                for (String message:AutoRoom.autoTextOutput) {
                    player.addChatMessage(new ChatComponentText(message));
                }
            } else {
                int top = Utils.dungeonTop(x,y,z);
                String blockFrequencies = Utils.blockFrequency(x,top,z, true);
                String size = Utils.getSize(x,top,z);
                String MD5 = Utils.getMD5(blockFrequencies);
                String floorFrequencies = Utils.floorFrequency(x, top, z);
                String floorHash = Utils.getMD5(floorFrequencies);

                switch (arg1[0].toLowerCase()) {
                    case "help":
                        player.addChatMessage(new ChatComponentText(
                                "\n" + EnumChatFormatting.GOLD + "Dungeon Rooms Mod Version " + DungeonRooms.VERSION + "\n" +
                                EnumChatFormatting.DARK_PURPLE + "Hotkeys: (Configurable in Controls Menu)\n" +
                                EnumChatFormatting.AQUA + " " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[1].getKeyCode()) + EnumChatFormatting.WHITE + " - Opens Secret Waypoints configuration GUI\n" +
                                EnumChatFormatting.AQUA + " " + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[0].getKeyCode()) + EnumChatFormatting.WHITE + " - (old) Opens images of secret locations\n" +
                                EnumChatFormatting.DARK_PURPLE + "Commands:\n" +
                                EnumChatFormatting.AQUA + " /room" + EnumChatFormatting.WHITE + " - Tells you in chat what room you are standing in.\n" +
                                EnumChatFormatting.AQUA + " /room help" + EnumChatFormatting.WHITE + " - Displays this message.\n" +
                                EnumChatFormatting.AQUA + " /room waypoints" + EnumChatFormatting.WHITE + " - Opens Secret Waypoints config GUI, alternatively can be opened with hotkey\n" +
                                EnumChatFormatting.AQUA + " /room move <x> <y>" + EnumChatFormatting.WHITE + " - Moves the GUI room name text to a coordinate. <x> and <y> are numbers between 0 and 100. Default is 50 for <x> and 5 for <y>.\n" +
                                EnumChatFormatting.AQUA + " /room toggle [argument]" + EnumChatFormatting.WHITE + " - Run \"/room toggle help\" for full list of toggles.\n" +
                                EnumChatFormatting.AQUA + " /room set <gui | dsg | sbp>" + EnumChatFormatting.WHITE + " - Configure whether the hotkey opens the selector GUI or directly goes to DSG/SBP.\n" +
                                EnumChatFormatting.AQUA + " /room discord" + EnumChatFormatting.WHITE + " - Opens the Discord invite for this mod in your browser.\n" /* +
                                EnumChatFormatting.AQUA + " /room open" + EnumChatFormatting.WHITE + " - Opens the gui for opening either DSG or SBP.\n" +
                                EnumChatFormatting.AQUA + " /room dsg" + EnumChatFormatting.WHITE + " - Directly opens DSG in the Discord client.\n" +
                                EnumChatFormatting.AQUA + " /room sbp" + EnumChatFormatting.WHITE + " - Directly opens the SBP secrets (if you have the mod installed).\n" */
                        ));
                        break;

                    case "gui":
                    case "open":
                        if (!Utils.inDungeons) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Use this command in dungeons"));
                            return;
                        }
                        OpenLink.checkForLink("gui");
                        break;

                    case "dsg":
                        if (!Utils.inDungeons) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Use this command in dungeons"));
                            return;
                        }
                        OpenLink.checkForLink("dsg");
                        break;

                    case "sbp":
                        if (!Utils.inDungeons) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Use this command in dungeons"));
                            return;
                        }
                        OpenLink.checkForLink("sbp");
                        break;

                    case "set":
                        switch (arg1[1].toLowerCase()) {
                            case "gui":
                                DungeonRooms.hotkeyOpen = "gui";
                                player.addChatMessage(new ChatComponentText("Hotkey has been set to open: GUI"));
                                ConfigHandler.writeStringConfig("gui", "hotkeyOpen", "gui");
                                break;
                            case "dsg":
                                DungeonRooms.hotkeyOpen = "dsg";
                                player.addChatMessage(new ChatComponentText("Hotkey has been set to open: DSG"));
                                ConfigHandler.writeStringConfig("gui", "hotkeyOpen", "dsg");
                                break;
                            case "sbp":
                                DungeonRooms.hotkeyOpen = "sbp";
                                player.addChatMessage(new ChatComponentText("Hotkey has been set to open: SBP"));
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
                        DungeonRooms.guiToOpen = "waypoints";
                        break;

                    case "move":
                        AutoRoom.scaleX = Integer.parseInt(arg1[1]);
                        AutoRoom.scaleY  = Integer.parseInt(arg1[2]);
                        ConfigHandler.writeIntConfig("gui", "scaleX", AutoRoom.scaleX);
                        ConfigHandler.writeIntConfig("gui", "scaleY", AutoRoom.scaleY);
                        player.addChatMessage(new ChatComponentText("Room GUI has been moved to " + arg1[1] + ", " + arg1[2]));
                        break;

                    case "toggle":
                        String toggleHelp = "\n" + EnumChatFormatting.GOLD + " Dungeon Rooms Mod Toggle Commands:" + "\n" +
                                EnumChatFormatting.AQUA + " /room toggle gui" + EnumChatFormatting.WHITE + " - Toggles displaying current room in gui.\n" +
                                EnumChatFormatting.AQUA + " /room toggle chat" + EnumChatFormatting.WHITE + " - Toggles writing current room name in chat.\n" +
                                EnumChatFormatting.AQUA + " /room toggle waypointtext" + EnumChatFormatting.WHITE + " - Toggles displaying waypoint names above waypoints.\n" +
                                EnumChatFormatting.AQUA + " /room toggle waypointboundingbox" + EnumChatFormatting.WHITE + " - Toggles displaying the bounding box on waypoints.\n" +
                                EnumChatFormatting.AQUA + " /room toggle waypointbeacon" + EnumChatFormatting.WHITE + " - Toggles displaying the beacon above waypoints.\n";
                        if (arg1.length == 1) {
                            player.addChatMessage(new ChatComponentText(toggleHelp));
                            break;
                        } else {
                            switch (arg1[1].toLowerCase()) {
                                case "help":
                                    player.addChatMessage(new ChatComponentText(toggleHelp));
                                    break;

                                case "gui":
                                    AutoRoom.guiToggled = !AutoRoom.guiToggled;
                                    ConfigHandler.writeBooleanConfig("toggles", "guiToggled", AutoRoom.guiToggled);
                                    player.addChatMessage(new ChatComponentText("Display room names in GUI has been set to: " + AutoRoom.guiToggled));
                                    break;

                                case "chat":
                                    AutoRoom.chatToggled = !AutoRoom.chatToggled;
                                    ConfigHandler.writeBooleanConfig("toggles", "chatToggled", AutoRoom.chatToggled);
                                    player.addChatMessage(new ChatComponentText("Display room names in Chat has been set to: " + AutoRoom.chatToggled));
                                    break;

                                case "text":
                                case "waypointtext":
                                    Waypoints.showWaypointText = !Waypoints.showWaypointText;
                                    ConfigHandler.writeBooleanConfig("waypoint", "showWaypointText", Waypoints.showWaypointText);
                                    player.addChatMessage(new ChatComponentText("Show Waypoint Text has been set to: " + Waypoints.showWaypointText));
                                    break;

                                case "boundingbox":
                                case "waypointboundingbox":
                                    Waypoints.showBoundingBox = !Waypoints.showBoundingBox;
                                    ConfigHandler.writeBooleanConfig("waypoint", "showBoundingBox", Waypoints.showBoundingBox);
                                    player.addChatMessage(new ChatComponentText("Show Waypoint Bounding Box has been set to: " + Waypoints.showBoundingBox));
                                    break;

                                case "beacon":
                                case "waypointbeacon":
                                    Waypoints.showBeacon = !Waypoints.showBeacon;
                                    ConfigHandler.writeBooleanConfig("waypoint", "showBeacon", Waypoints.showBeacon);
                                    player.addChatMessage(new ChatComponentText("Show Waypoint Beacon has been set to: " + Waypoints.showBeacon));
                                    break;

                                case "dev":
                                case "coord":
                                    AutoRoom.coordToggled = !AutoRoom.coordToggled;
                                    ConfigHandler.writeBooleanConfig("toggles", "coordToggled", AutoRoom.coordToggled);
                                    player.addChatMessage(new ChatComponentText("Display dev coords has been set to: " + AutoRoom.coordToggled));
                                    break;

                                case "override":
                                    Utils.dungeonOverride = !Utils.dungeonOverride;
                                    player.addChatMessage(new ChatComponentText("Force inDungeons has been set to: " + Utils.dungeonOverride));
                                    break;

                                default:
                                    player.addChatMessage(new ChatComponentText(toggleHelp));
                                    break;
                            }
                        }
                        break;

                    case "reload":
                        ConfigHandler.reloadConfig();
                        player.addChatMessage(new ChatComponentText("Reloaded config file"));
                        break;

                    case "discord":
                        try {
                            player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening Dungeon Rooms Discord invite in browser..."));
                            Desktop.getDesktop().browse(new URI("https://discord.gg/7B5RbsArYK"));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "json":
                        if (!Utils.inDungeons) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Use this command in dungeons"));
                            return;
                        }
                        if (DungeonRooms.roomsJson.get(MD5) != null) {
                            if (MD5.equals("16370f79b2cad049096f881d5294aee6") && !floorHash.equals("94fb12c91c4b46bd0c254edadaa49a3d")) {
                                floorHash = "e617eff1d7b77faf0f8dd53ec93a220f"; //exception for box room because floorhash changes when you walk on it
                            }

                            int arraySize = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().size();
                            if (arraySize >= 2) {
                                boolean floorHashFound = false;
                                List<String> chatMessages = new ArrayList<>();

                                for(int i = 0; i < arraySize; i++){
                                    JsonElement jsonFloorHash = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().get("floorhash");
                                    if (floorHash != null && jsonFloorHash != null){
                                        if (floorHash.equals(jsonFloorHash.getAsString())){
                                            String json = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().toString();
                                            player.addChatMessage(new ChatComponentText(json));
                                            floorHashFound = true;
                                        }
                                    } else {
                                        String json = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(i).getAsJsonObject().toString();
                                        chatMessages.add(json);
                                    }
                                }
                                if (!floorHashFound) {
                                    for (String json:chatMessages) {
                                        player.addChatMessage(new ChatComponentText(json));
                                    }
                                }
                            } else {
                                String json = DungeonRooms.roomsJson.get(MD5).getAsJsonArray().get(0).getAsJsonObject().toString();
                                player.addChatMessage(new ChatComponentText(json));
                            }
                        }
                        break;

                    //For adding room info
                    case "copy":
                        if (!Utils.inDungeons) return;
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + Utils.getDimensions(x, top, z)));
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + size + " " + MD5));
                        break;

                    case "copyfloor":
                        if (!Utils.inDungeons) return;
                        player.addChatMessage(new ChatComponentText( EnumChatFormatting.GREEN + "floorhash " + floorHash));
                        break;

                    case "dev":
                        player.addChatMessage(new ChatComponentText("dev: size = " + size));
                        player.addChatMessage(new ChatComponentText("dev: MD5 = " + MD5));
                        player.addChatMessage(new ChatComponentText("dev: floorhash = " + floorHash));
                        break;

                    case "coord":
                        if(Utils.originBlock == null) {
                            DungeonRooms.logger.warn("DungeonRooms: originBlock is null");
                            return;
                        }
                        BlockPos relativeCoord = Utils.actualToRelative(new BlockPos(player.posX,player.posY,player.posZ));
                        if (relativeCoord == null) return;

                        player.addChatMessage(new ChatComponentText("Origin: " + Utils.originBlock.getX() + "," + Utils.originBlock.getY() + "," + Utils.originBlock.getZ()));
                        player.addChatMessage(new ChatComponentText("Relative Pos.: "+ relativeCoord.getX() + "," + relativeCoord.getY() + "," + relativeCoord.getZ()));
                        break;

                    case "add":
                        Minecraft mc = Minecraft.getMinecraft();
                        World world = mc.theWorld;
                        switch (arg1[1].toLowerCase()) {
                            case "chest":
                                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                                    BlockPos viewingPos = Utils.actualToRelative(mc.objectMouseOver.getBlockPos());
                                    if (viewingPos == null) return;
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
                                    BlockPos viewingPos = Utils.actualToRelative(mc.objectMouseOver.getBlockPos());
                                    if (viewingPos == null) return;
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
                                    BlockPos viewingPos = Utils.actualToRelative(mc.objectMouseOver.getBlockPos());
                                    if (viewingPos == null) return;
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
                                    BlockPos viewingPos = Utils.actualToRelative(mc.objectMouseOver.getBlockPos());
                                    if (viewingPos == null) return;
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
                                    BlockPos viewingPos = Utils.actualToRelative(mc.objectMouseOver.getBlockPos().up(1));
                                    if (viewingPos == null) return;
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
                                BlockPos playerPos = Utils.actualToRelative(new BlockPos(player.posX,player.posY,player.posZ));
                                if (playerPos == null) return;
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
                                BlockPos entrancePos = Utils.actualToRelative(new BlockPos(player.posX,player.posY+1,player.posZ));
                                if (entrancePos == null) return;
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
                                BlockPos batPos = Utils.actualToRelative(new BlockPos(player.posX,player.posY+1,player.posZ));
                                if (batPos == null) return;
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
                                        + "Dungeon Rooms: Valid options are <chest | wither | superboom | lever | fairysoul | item | entrance | bat>"));
                                break;
                        }
                        break;
                    default:
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Dungeon Rooms: Run \"/room\" by itself to see the room name or run \"/room help\" for additional options"));
                }
            }
        }).start();
    }
}
