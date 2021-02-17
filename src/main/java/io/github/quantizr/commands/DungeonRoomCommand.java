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
import io.github.quantizr.handlers.ConfigHandler;
import io.github.quantizr.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Collections;
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
            return getListOfStringsMatchingLastWord(args, "togglechat", "togglegui", "move");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        new Thread(() -> {
            EntityPlayer player = (EntityPlayer) arg0;

            int x = (int) Math.floor(player.posX);
            int y = (int) Math.floor(player.posY);
            int z = (int) Math.floor(player.posZ);

            if (arg1.length < 1) {
                if (!Utils.inDungeons) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: Use this command in dungeons"));
                    return;
                }
                int top = Utils.dungeonTop(x,y,z);
                String blockFrequencies = Utils.blockFrequency(x,top,z);
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
                String blockFrequencies = Utils.blockFrequency(x,top,z);
                String size = Utils.getSize(x,top,z);
                String MD5 = Utils.getMD5(blockFrequencies);
                String floorFrequencies = Utils.floorFrequency(x, top, z);
                String floorHash = Utils.getMD5(floorFrequencies);

                switch (arg1[0].toLowerCase()) {
                    case "help":
                        player.addChatMessage(new ChatComponentText("\n" + EnumChatFormatting.GOLD + " Dungeon Rooms Mod Version " + DungeonRooms.VERSION + "\n" +
                                EnumChatFormatting.DARK_PURPLE + " /room" + EnumChatFormatting.AQUA + " - Tells you in chat what room you are standing in.\n" +
                                EnumChatFormatting.DARK_PURPLE + " /room help" + EnumChatFormatting.AQUA + " - Displays this message.\n" +
                                EnumChatFormatting.DARK_PURPLE + " /room move <x> <y>" + EnumChatFormatting.AQUA + " - Moves the GUI room name display to a coordinate. <x> and <y> are numbers between 0 and 100. Default is 50 for <x> and 5 for <y>.\n" +
                                EnumChatFormatting.DARK_PURPLE + " /room togglegui" + EnumChatFormatting.AQUA + " - Toggles whether room name is automatically displayed in GUI. Default is on.\n" +
                                EnumChatFormatting.DARK_PURPLE + " /room togglechat" + EnumChatFormatting.AQUA + " - Toggles whether room name is automatically displayed in Chat. Default is off.\n"));
                        break;

                    case "toggle":
                        player.addChatMessage(new ChatComponentText("Run either /room togglegui or /room togglechat"));
                        break;

                    case "togglegui":
                        AutoRoom.guiToggled = !AutoRoom.guiToggled;
                        ConfigHandler.writeBooleanConfig("toggles", "guiToggled", AutoRoom.guiToggled);
                        player.addChatMessage(new ChatComponentText("Display room names in GUI has been set to: " + AutoRoom.guiToggled));
                        break;

                    case "togglechat":
                        AutoRoom.chatToggled = !AutoRoom.chatToggled;
                        ConfigHandler.writeBooleanConfig("toggles", "chatToggled", AutoRoom.chatToggled);
                        player.addChatMessage(new ChatComponentText("Display room names in Chat has been set to: " + AutoRoom.chatToggled));
                        break;

                    case "move":
                        AutoRoom.scaleX = Integer.parseInt(arg1[1]);
                        AutoRoom.scaleY  = Integer.parseInt(arg1[2]);
                        ConfigHandler.writeIntConfig("gui", "scaleX", AutoRoom.scaleX);
                        ConfigHandler.writeIntConfig("gui", "scaleY", AutoRoom.scaleY);
                        player.addChatMessage(new ChatComponentText("Room GUI have been moved to " + arg1[1] + ", " + arg1[2]));
                        break;

                    case "json":
                        if (!Utils.inDungeons) {
                            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                    + "Dungeon Rooms: Use this command in dungeons"));
                            return;
                        }
                        if (DungeonRooms.roomsJson.get(MD5) == null) {
                            player.addChatMessage(new ChatComponentText("null"));
                        } else {
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
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + size + " " + MD5));
                        break;

                    case "copydupe":
                        if (!Utils.inDungeons) return;
                        player.addChatMessage(new ChatComponentText( EnumChatFormatting.GREEN + "duplicate " + floorHash));
                        break;

                    default:
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                                + "Run \"/room\" by itself to see the room name or run \"/room help\" for additional options"));
                }
            }
        }).start();
    }
}
