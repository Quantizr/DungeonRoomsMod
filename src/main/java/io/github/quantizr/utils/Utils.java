/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.utils;

import com.google.gson.JsonElement;
import io.github.quantizr.DungeonRooms;
import io.github.quantizr.handlers.ScoreboardHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {
    /*
    checkForSkyblock and checkForDungeons were taken from Danker's Skyblock Mod (https://github.com/bowser0000/SkyblockMod/).
    Those methods were released under GNU General Public License v3.0 and remains under said license.
    Modified by Quantizr (_risk) in Feb. 2021.
    */
    public static boolean inSkyblock = false;
    public static boolean inDungeons = false;
    public static boolean dungeonOverride = false;

    public static BlockPos originBlock = null;
    public static String originCorner = null;

    public static void checkForSkyblock() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null && !mc.isSingleplayer()) {
            ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (scoreboardObj != null) {
                String scObjName = ScoreboardHandler.cleanSB(scoreboardObj.getDisplayName());
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true;
                    return;
                }
            }
        }
        inSkyblock = false;
    }

    public static void checkForDungeons() {
        if (dungeonOverride) {
            inDungeons = true;
            return;
        }
        if (inSkyblock) {
            List<String> scoreboard = ScoreboardHandler.getSidebarLines();
            for (String s : scoreboard) {
                String sCleaned = ScoreboardHandler.cleanSB(s);
                if (sCleaned.contains("The Catacombs")) {
                    inDungeons = true;
                    return;
                }
            }
        }
        inDungeons = false;
    }

    public static int dungeonTop(double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        for (int i = 255; i >= 78; i--) { //start at height limit, go down to 10 blocks above floor
            Block top = world.getBlockState(new BlockPos(x,i,z)).getBlock();
            if (top != Blocks.air){
                if (checkPlatform(x,i,z)) return i;
            }
        }

        //if top not found
        return -1;
    }

    public static int dungeonBottom(double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        for (int i = 0; i <= 68; i++) { //start at height limit, go down to floor level
            Block bottom = world.getBlockState(new BlockPos(x,i,z)).getBlock();
            if (bottom == Blocks.bedrock || bottom == Blocks.stone){
                return i;
            }
        }

        //if top not found
        return -1;
    }

    public static int dungeonHeight(double x, double z) {
        return dungeonTop(x,68,z) - dungeonBottom(x,68,z);
    }

    public static boolean checkPlatform(double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        int checkedNorth = 0;
        int checkedSouth = 0;
        int checkedEast = 0;
        int checkedWest = 0;

        for (int j = 0; j < 10; j++){
            Block checkNorth = world.getBlockState(new BlockPos(x,y,z-j)).getBlock();
            if (checkNorth != Blocks.air) checkedNorth++;
            Block checkSouth = world.getBlockState(new BlockPos(x,y,z+j)).getBlock();
            if (checkSouth != Blocks.air) checkedSouth++;
            Block checkEast = world.getBlockState(new BlockPos(x+j,y,z)).getBlock();
            if (checkEast != Blocks.air) checkedEast++;
            Block checkWest = world.getBlockState(new BlockPos(x-j,y,z)).getBlock();
            if (checkWest != Blocks.air) checkedWest++;
        }

        return (checkedNorth == 10 || checkedSouth == 10 || checkedEast == 10 || checkedWest == 10);
    }

    public static int endOfRoom(int x, int y, int z, String direction) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        switch (direction) {
            case "n":
                for (int i = 1; i <= 200; i++){
                    Block northEnd = world.getBlockState(new BlockPos(x,y,z-i)).getBlock();
                    if (northEnd == Blocks.air || checkPlatform(x,y+1,z-i)
                            || Math.abs(dungeonHeight(x,z-i) - dungeonHeight(x,z-i+1)) > 3) return (z-i+1);
                }
                break;
            case "s":
                for (int i = 1; i <= 200; i++){
                    Block southEnd = world.getBlockState(new BlockPos(x,y,z+i)).getBlock();
                    if (southEnd == Blocks.air || checkPlatform(x,y+1,z+i)
                            || Math.abs(dungeonHeight(x,z+i) - dungeonHeight(x,z+i-1)) > 3) return (z+i-1);
                }
                break;
            case "e":
                for (int i = 1; i <= 200; i++){
                    Block eastEnd = world.getBlockState(new BlockPos(x+i,y,z)).getBlock();
                    if (eastEnd == Blocks.air || checkPlatform(x+i,y+1,z)
                            || Math.abs(dungeonHeight(x+i,z) - dungeonHeight(x+i-1,z)) > 3) return (x+i-1);
                }
                break;
            case "w":
                for (int i = 1; i <= 200; i++){
                    Block westEnd = world.getBlockState(new BlockPos(x-i,y,z)).getBlock();
                    if (westEnd == Blocks.air || checkPlatform(x-i,y+1,z)
                            || Math.abs(dungeonHeight(x-i,z) - dungeonHeight(x-i+1,z)) > 3) return (x-i+1);
                }
                break;
        }
        return -1;
    }

    public static int northWidth(int x, int y, int z) {
        int northZ = endOfRoom(x,y,z,"n");
        return endOfRoom(x,y,northZ, "e") - endOfRoom(x,y,northZ, "w");
    }

    public static int southWidth(int x, int y, int z) {
        int southZ = endOfRoom(x,y,z,"s");
        return endOfRoom(x,y,southZ, "e") - endOfRoom(x,y,southZ, "w");
    }

    public static int eastWidth(int x, int y, int z) {
        int eastX = endOfRoom(x,y,z,"e");
        return endOfRoom(eastX,y,z, "s") - endOfRoom(eastX,y,z, "n");
    }

    public static int westWidth(int x, int y, int z) {
        int westX = endOfRoom(x,y,z,"w");
        return endOfRoom(westX,y,z, "s") - endOfRoom(westX,y,z, "n");
    }

    public static String getDimensions(int x, int y, int z) {
        return "n:" + northWidth(x,y,z) + " s:" + southWidth(x,y,z) + " e:" + eastWidth(x,y,z) + " w:" + westWidth(x,y,z);
    }

    public static String getSize(int x, int y, int z) {
        int n = northWidth(x,y,z);
        int s = southWidth(x,y,z);
        int e = eastWidth(x,y,z);
        int w = westWidth(x,y,z);

        if (n == s && s == e && e == w){ //square
            if(n == 30) return "1x1";
            if (n == 62) return "2x2";
        } else if (n == s && e == w){ //rectangle that isn't square
            if((n == 62 && e == 30) || (n == 30 && e == 62)) return "1x2";
            if((n == 94 && e == 30) || (n == 30 && e == 94)) return "1x3";
            if((n == 126 && e == 30) || (n == 30 && e == 126)) return "1x4";
        } else if ((n != s) || (e != w)) {
            int length62 = (n==62?1:0)+(s==62?1:0)+(e==62?1:0)+(w==62?1:0); //# of sides length 62
            int length30 = (n==30?1:0)+(s==30?1:0)+(e==30?1:0)+(w==30?1:0); //# of sides length 30
            if (length62 >= 2 && length30 == 4-length62) return "L-shape";
        }
        //else
        return "error";
    }


    public static String blockFrequency(int x, int y, int z, boolean isPlayerPos){
        if (y == -1) return null;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        List<String> blockList = new ArrayList<>();
        List<String> frequencies = new ArrayList<>();

        if (northWidth(x,y,z) == southWidth(x,y,z)) {
            if (eastWidth(x,y,z) == westWidth(x,y,z)) {
                //rectangle shape
                int northEndZ = endOfRoom(x,y,z,"n");
                int northWestEndX = endOfRoom(x,y,northEndZ,"w");
                int southEndZ = endOfRoom(x,y,z,"s");
                int southEastEndX = endOfRoom(x,y,southEndZ,"e");

                BlockPos northWestCorner = new BlockPos(northWestEndX, y, northEndZ);
                BlockPos southEastCorner = new BlockPos(southEastEndX, y, southEndZ);

                Iterable<BlockPos> blocks = BlockPos.getAllInBox(northWestCorner, southEastCorner);
                for (BlockPos blockPos : blocks) {
                    if (isPlayerPos) checkCorner(blockPos);
                    blockList.add(world.getBlockState(blockPos).toString());
                }

            } else if (getSize(x,y,z).equals("L-shape")) {
                //L-shape East/West not equal
                if (eastWidth(x,y,z) > westWidth(x,y,z)) {
                    int eastEndX = endOfRoom(x,y,z,"e");
                    int northEndZ = endOfRoom(eastEndX, y, z,"n");
                    for (int i = 0; i < 200; i++) {
                        Block nextColumn = world.getBlockState(new BlockPos(eastEndX,y,northEndZ+i)).getBlock();
                        if (nextColumn == Blocks.air || checkPlatform(eastEndX,y+1,northEndZ+i)
                                || (i>0 && Math.abs(dungeonHeight(eastEndX, northEndZ+i) - dungeonHeight(eastEndX, northEndZ+i-1)) > 3)) break;

                        for (int j = 0; j < 200; j++) {
                            BlockPos nextBlockPos = new BlockPos(eastEndX-j,y,northEndZ+i);
                            Block nextBlock = world.getBlockState(nextBlockPos).getBlock();
                            if (nextBlock == Blocks.air || checkPlatform(eastEndX-j,y+1,northEndZ+i)
                                    || (j>0 && Math.abs(dungeonHeight(eastEndX-j, northEndZ+i) - dungeonHeight(eastEndX-j+1, northEndZ+i)) > 3)) break;

                            if (isPlayerPos) checkCorner(nextBlockPos);
                            blockList.add(nextBlock.toString());
                        }
                    }
                } else if (westWidth(x,y,z) > eastWidth(x,y,z)) {
                    int westEndX = endOfRoom(x, y, z, "w");
                    int northEndZ = endOfRoom(westEndX, y, z, "n");
                    for (int i = 0; i < 200; i++) {
                        Block nextColumn = world.getBlockState(new BlockPos(westEndX,y,northEndZ+i)).getBlock();
                        if (nextColumn == Blocks.air || checkPlatform(westEndX,y+1,northEndZ+i)
                                || (i>0 && Math.abs(dungeonHeight(westEndX, northEndZ+i) - dungeonHeight(westEndX, northEndZ+i-1)) > 3)) break;

                        for (int j = 0; j < 200; j++) {
                            BlockPos nextBlockPos = new BlockPos(westEndX+j,y,northEndZ+i);
                            Block nextBlock = world.getBlockState(nextBlockPos).getBlock();
                            if (nextBlock == Blocks.air || checkPlatform(westEndX+j,y+1,northEndZ+i)
                                    || (j>0 && Math.abs(dungeonHeight(westEndX+j, northEndZ+i) - dungeonHeight(westEndX+j-1, northEndZ+i)) > 3)) break;

                            if (isPlayerPos) checkCorner(nextBlockPos);
                            blockList.add(nextBlock.toString());
                        }
                    }
                }
            }
        } else if (getSize(x,y,z).equals("L-shape")) {
            //L-shape North/South not equal
            if (northWidth(x,y,z) > southWidth(x,y,z)) {
                int northEndZ = endOfRoom(x,y,z,"n");
                int westEndX = endOfRoom(x,y,northEndZ,"w");
                for (int i = 0; i < 200; i++) {
                    Block nextColumn = world.getBlockState(new BlockPos(westEndX+i,y,northEndZ)).getBlock();
                    if (nextColumn == Blocks.air || checkPlatform(westEndX+i,y+1,northEndZ)
                            || (i>0 && Math.abs(dungeonHeight(westEndX+i, northEndZ) - dungeonHeight(westEndX+i-1, northEndZ)) > 3)) break;

                    for (int j = 0; j < 200; j++) {
                        BlockPos nextBlockPos = new BlockPos(westEndX+i,y,northEndZ+j);
                        Block nextBlock = world.getBlockState(nextBlockPos).getBlock();
                        if (nextBlock == Blocks.air || checkPlatform(westEndX+i,y+1,northEndZ+j)
                                || (j>0 && Math.abs(dungeonHeight(westEndX+i, northEndZ+j) - dungeonHeight(westEndX+i, northEndZ+j-1)) > 3)) break;

                        if (isPlayerPos) checkCorner(nextBlockPos);
                        blockList.add(nextBlock.toString());
                    }
                }
            } else if (southWidth(x,y,z) > northWidth(x,y,z)) {
                int southEndZ = endOfRoom(x,y,z,"s");
                int westEndX = endOfRoom(x,y,southEndZ,"w");
                for (int i = 0; i < 200; i++) {
                    Block nextColumn = world.getBlockState(new BlockPos(westEndX+i,y,southEndZ)).getBlock();
                    if (nextColumn == Blocks.air || checkPlatform(westEndX+i,y+1,southEndZ)
                            || (i>0 && Math.abs(dungeonHeight(westEndX+i, southEndZ) - dungeonHeight(westEndX+i-1, southEndZ)) > 3)) break;

                    for (int j = 0; j < 200; j++) {
                        BlockPos nextBlockPos = new BlockPos(westEndX+i,y,southEndZ-j);
                        Block nextBlock = world.getBlockState(nextBlockPos).getBlock();
                        if (nextBlock == Blocks.air || checkPlatform(westEndX+i,y+1,southEndZ-j)
                                || (j>0 && Math.abs(dungeonHeight(westEndX+i, southEndZ-j) - dungeonHeight(westEndX+i, southEndZ-j+1)) > 3)) break;

                        if (isPlayerPos) checkCorner(nextBlockPos);
                        blockList.add(nextBlock.toString());
                    }
                }
            }
        }
        if (blockList.isEmpty()) return null;

        Set<String> distinct = new HashSet<>(blockList);
        for (String s: distinct) {
            frequencies.add(s + ":" + Collections.frequency(blockList, s));
        }
        Collections.sort(frequencies);
        return String.join(",", frequencies);
    }

    public static void checkCorner(BlockPos blockPos) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        if (world.getBlockState(blockPos).getBlock() == Blocks.stained_hardened_clay) {
            Block northBlock = world.getBlockState(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()-1)).getBlock();
            Block southBlock = world.getBlockState(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()+1)).getBlock();
            Block eastBlock = world.getBlockState(new BlockPos(blockPos.getX()+1, blockPos.getY(), blockPos.getZ())).getBlock();
            Block westBlock = world.getBlockState(new BlockPos(blockPos.getX()-1, blockPos.getY(), blockPos.getZ())).getBlock();
            if (northBlock == Blocks.air && southBlock != Blocks.air && eastBlock != Blocks.air && westBlock == Blocks.air) {
                originCorner = "northwest";
                originBlock = blockPos;
            } else if (northBlock == Blocks.air && southBlock != Blocks.air && eastBlock == Blocks.air && westBlock != Blocks.air) {
                originCorner = "northeast";
                originBlock = blockPos;
            } else if (northBlock != Blocks.air && southBlock == Blocks.air && eastBlock == Blocks.air && westBlock != Blocks.air) {
                originCorner = "southeast";
                originBlock = blockPos;
            } else if (northBlock != Blocks.air && southBlock == Blocks.air && eastBlock != Blocks.air && westBlock == Blocks.air) {
                originCorner = "southwest";
                originBlock = blockPos;
            }
        }
    }

    public static String floorFrequency(int x, int y, int z) {
        if (y == -1) return null;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        List<String> blockList = new ArrayList<>();
        List<String> frequencies = new ArrayList<>();

        if (northWidth(x, y, z) == southWidth(x, y, z)) {
            if (eastWidth(x, y, z) == westWidth(x, y, z)) {
                int northEndZ = endOfRoom(x, y, z, "n");
                int northWestEndX = endOfRoom(x, y, northEndZ, "w");
                int southEndZ = endOfRoom(x, y, z, "s");
                int southEastEndX = endOfRoom(x, y, southEndZ, "e");

                BlockPos northWestCorner = new BlockPos(northWestEndX+10, 68, northEndZ+10);
                BlockPos southEastCorner = new BlockPos(southEastEndX-10, 68, southEndZ-10);

                Iterable<BlockPos> blocks = BlockPos.getAllInBox(northWestCorner, southEastCorner);
                for (BlockPos blockPos : blocks) {
                    blockList.add(world.getBlockState(blockPos).getBlock().toString());
                }
            }
        }
        if (getSize(x,y,z).equals("L-shape")) {
            blockList.add(String.valueOf(dungeonTop(x,68,z)));
        }

        if (blockList.isEmpty()) return null;

        Set<String> distinct = new HashSet<>(blockList);
        for (String s: distinct) {
            frequencies.add(s + ":" + Collections.frequency(blockList, s));
        }
        Collections.sort(frequencies);
        return String.join(",", frequencies);
    }

    public static String getMD5(String input) {
        try {
            if (input == null) return null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockPos actualToRelative(BlockPos actual) {
        if (Utils.originBlock == null || Utils.originCorner == null) return null;
        double x = 0;
        double z = 0;
        switch (Utils.originCorner) {
            case "northwest":
                x = actual.getX() - Utils.originBlock.getX();
                z = actual.getZ() - Utils.originBlock.getZ();
                break;
            case "northeast":
                x = actual.getZ() - Utils.originBlock.getZ();
                z = -(actual.getX() - Utils.originBlock.getX());
                break;
            case "southeast":
                x = -(actual.getX() - Utils.originBlock.getX());
                z = -(actual.getZ() - Utils.originBlock.getZ());
                break;
            case "southwest":
                x = -(actual.getZ() - Utils.originBlock.getZ());
                z = actual.getX() - Utils.originBlock.getX();
                break;
        }
        return new BlockPos(x, actual.getY(), z);
    }

    public static BlockPos relativeToActual(BlockPos relative) {
        if (Utils.originBlock == null || Utils.originCorner == null) return null;
        double x = 0;
        double z = 0;
        switch (Utils.originCorner) {
            case "northwest":
                x = relative.getX() + Utils.originBlock.getX();
                z = relative.getZ() + Utils.originBlock.getZ();
                break;
            case "northeast":
                x = -(relative.getZ() - Utils.originBlock.getX());
                z = relative.getX() + Utils.originBlock.getZ();
                break;
            case "southeast":
                x = -(relative.getX() - Utils.originBlock.getX());
                z = -(relative.getZ() - Utils.originBlock.getZ());
                break;
            case "southwest":
                x = relative.getZ() + Utils.originBlock.getX();
                z = -(relative.getX() - Utils.originBlock.getZ());
                break;
        }
        return new BlockPos(x, relative.getY(), z);
    }

    public static List<String> roomList() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        List<String> nameList = new ArrayList<>();
        if (!inDungeons) return nameList;
        for (int j = -8; j <= 8; j++) {
            for (int k = -8; k <= 8; k++) {
                int scanX = (int)player.posX + (32*j);
                int scanZ = (int)player.posZ + (32*k);
                int scanTop = Utils.dungeonTop(scanX,68,scanZ);
                String scanBlockFrequencies = Utils.blockFrequency(scanX,scanTop,scanZ,false);
                if (scanBlockFrequencies == null) continue;
                String scanMD5 = Utils.getMD5(scanBlockFrequencies);
                String scanFloorFrequencies = Utils.floorFrequency(scanX,scanTop,scanZ);
                if (scanFloorFrequencies == null) continue;
                String scanFloorHash = Utils.getMD5(scanFloorFrequencies);

                if (DungeonRooms.roomsJson.get(scanMD5) != null) {
                    if (scanMD5.equals("16370f79b2cad049096f881d5294aee6") && !scanFloorHash.equals("94fb12c91c4b46bd0c254edadaa49a3d")) {
                        scanFloorHash = "e617eff1d7b77faf0f8dd53ec93a220f"; //exception for box room because floorhash changes when you walk on it
                    }

                    int arraySize = DungeonRooms.roomsJson.get(scanMD5).getAsJsonArray().size();
                    if (arraySize >= 2) {
                        boolean floorHashFound = false;
                        List<String> chatMessages = new ArrayList<>();

                        for(int i = 0; i < arraySize; i++){
                            JsonElement jsonFloorHash = DungeonRooms.roomsJson.get(scanMD5).getAsJsonArray().get(i).getAsJsonObject().get("floorhash");
                            if (scanFloorHash != null && jsonFloorHash != null){
                                if (scanFloorHash.equals(jsonFloorHash.getAsString())){
                                    String name = DungeonRooms.roomsJson.get(scanMD5).getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString();
                                    nameList.add(name);
                                    floorHashFound = true;
                                }
                            } else {
                                String name = DungeonRooms.roomsJson.get(scanMD5).getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString();
                                chatMessages.add(name);
                            }
                        }
                        if (!floorHashFound) {
                            nameList.addAll(chatMessages);
                        }
                    } else {
                        String name = DungeonRooms.roomsJson.get(scanMD5).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                        nameList.add(name);
                    }
                }
            }
        }
        Set<String> nameListSet = new HashSet<>(nameList);
        nameList.clear();
        nameList.addAll(nameListSet);
        return nameList;
    }

}
