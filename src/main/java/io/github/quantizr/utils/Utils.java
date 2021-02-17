/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr.utils;

import io.github.quantizr.handlers.ScoreboardHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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
        EntityPlayerSP player = mc.thePlayer;

        for (int i = 255; i >= y+2; i--) { //start at height limit, go down to 2 blocks above player
            Block top = world.getBlockState(new BlockPos(x,i,z)).getBlock();
            if (top != Blocks.air){
                int checkedNorth = 0;
                int checkedSouth = 0;
                int checkedEast = 0;
                int checkedWest = 0;

                for (int j = 1; j <= 8; j++){
                    Block checkNorth = world.getBlockState(new BlockPos(x,i,z-j)).getBlock();
                    if (checkNorth != Blocks.air) checkedNorth++;
                    Block checkSouth = world.getBlockState(new BlockPos(x,i,z+j)).getBlock();
                    if (checkSouth != Blocks.air) checkedSouth++;
                    Block checkEast = world.getBlockState(new BlockPos(x+j,i,z)).getBlock();
                    if (checkEast != Blocks.air) checkedEast++;
                    Block checkWest = world.getBlockState(new BlockPos(x-j,i,z)).getBlock();
                    if (checkWest != Blocks.air) checkedWest++;
                }

                if (checkedNorth==8 || checkedSouth == 8 || checkedEast == 8 || checkedWest == 8){
                    return i;
                } else {
                    System.out.println("Detected block was not large flat surface");
                }
            }
        }

        //if top not found
        return -1;
    }

    public static int endOfRoom(int x, int y, int z, String direction) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;

        switch (direction) {
            case "n":
                for (int i = 1; i <= 200; i++){
                    Block northEnd = world.getBlockState(new BlockPos(x,y,z-i)).getBlock();
                    Block northEndUpOne = world.getBlockState(new BlockPos(x,y+1,z-i)).getBlock(); //in case of rooms without air gap between *cough* *cough* F3 Entrance
                    if (northEnd == Blocks.air || (northEndUpOne != Blocks.air && northEndUpOne != Blocks.wool && northEndUpOne != Blocks.gold_block && northEndUpOne != Blocks.redstone_block && northEndUpOne != Blocks.standing_sign)) return (z-i+1);
                }
                break;
            case "s":
                for (int i = 1; i <= 200; i++){
                    Block southEnd = world.getBlockState(new BlockPos(x,y,z+i)).getBlock();
                    Block southEndUpOne = world.getBlockState(new BlockPos(x,y+1,z+i)).getBlock();
                    if (southEnd == Blocks.air || (southEndUpOne != Blocks.air && southEndUpOne != Blocks.wool && southEndUpOne != Blocks.gold_block && southEndUpOne != Blocks.redstone_block && southEndUpOne != Blocks.standing_sign)) return (z+i-1);
                }
                break;
            case "e":
                for (int i = 1; i <= 200; i++){
                    Block eastEnd = world.getBlockState(new BlockPos(x+i,y,z)).getBlock();
                    Block eastEndUpOne = world.getBlockState(new BlockPos(x+i,y+1,z)).getBlock();
                    if (eastEnd == Blocks.air || (eastEndUpOne != Blocks.air && eastEndUpOne != Blocks.wool && eastEndUpOne != Blocks.gold_block && eastEndUpOne != Blocks.redstone_block && eastEndUpOne != Blocks.standing_sign)) return (x+i-1);
                }
                break;
            case "w":
                for (int i = 1; i <= 200; i++){
                    Block westEnd = world.getBlockState(new BlockPos(x-i,y,z)).getBlock();
                    Block westEndUpOne = world.getBlockState(new BlockPos(x-i,y+1,z)).getBlock();
                    if (westEnd == Blocks.air || (westEndUpOne != Blocks.air && westEndUpOne != Blocks.wool && westEndUpOne != Blocks.gold_block && westEndUpOne != Blocks.redstone_block && westEndUpOne != Blocks.standing_sign)) return (x-i+1);
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


    public static String blockFrequency(int x, int y, int z){
        if (y == -1) return null;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;

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
                    blockList.add(world.getBlockState(blockPos).toString());
                }

            } else if (getSize(x,y,z).equals("L-shape")) {
                //L-shape East/West not equal
                if (eastWidth(x,y,z) > westWidth(x,y,z)) {
                    int eastEndX = endOfRoom(x,y,z,"e");
                    int northEndZ = endOfRoom(eastEndX, y, z,"n");
                    for (int i = 0; i < 200; i++) {
                        Block nextColumn = world.getBlockState(new BlockPos(eastEndX,y,northEndZ+i)).getBlock();
                        Block nextColumnUpOne = world.getBlockState(new BlockPos(eastEndX,y+1,northEndZ+i)).getBlock();
                        if (nextColumn == Blocks.air || (nextColumnUpOne != Blocks.air && nextColumnUpOne != Blocks.wool && nextColumnUpOne != Blocks.gold_block  && nextColumnUpOne != Blocks.redstone_block && nextColumnUpOne != Blocks.standing_sign)) break;

                        for (int j = 0; j < 200; j++) {
                            Block nextBlock = world.getBlockState(new BlockPos(eastEndX-j,y,northEndZ+i)).getBlock();
                            Block nextBlockUpOne = world.getBlockState(new BlockPos(eastEndX-j,y+1,northEndZ+i)).getBlock();
                            if (nextBlock == Blocks.air || (nextBlockUpOne != Blocks.air && nextBlockUpOne != Blocks.wool && nextBlockUpOne != Blocks.gold_block  && nextBlockUpOne != Blocks.redstone_block && nextBlockUpOne != Blocks.standing_sign)) break;

                            blockList.add(nextBlock.toString());
                        }
                    }
                } else if (westWidth(x,y,z) > eastWidth(x,y,z)) {
                    int westEndX = endOfRoom(x, y, z, "w");
                    int northEndZ = endOfRoom(westEndX, y, z, "n");
                    for (int i = 0; i < 200; i++) {
                        Block nextColumn = world.getBlockState(new BlockPos(westEndX, y, northEndZ + i)).getBlock();
                        Block nextColumnUpOne = world.getBlockState(new BlockPos(westEndX, y + 1, northEndZ + i)).getBlock();
                        if (nextColumn == Blocks.air || (nextColumnUpOne != Blocks.air && nextColumnUpOne != Blocks.wool && nextColumnUpOne != Blocks.gold_block  && nextColumnUpOne != Blocks.redstone_block && nextColumnUpOne != Blocks.standing_sign)) break;

                        for (int j = 0; j < 200; j++) {
                            Block nextBlock = world.getBlockState(new BlockPos(westEndX + j, y, northEndZ + i)).getBlock();
                            Block nextBlockUpOne = world.getBlockState(new BlockPos(westEndX + j, y + 1, northEndZ + i)).getBlock();
                            if (nextBlock == Blocks.air || (nextBlockUpOne != Blocks.air && nextBlockUpOne != Blocks.wool && nextBlockUpOne != Blocks.gold_block && nextBlockUpOne != Blocks.redstone_block && nextBlockUpOne != Blocks.standing_sign)) break;

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
                    Block nextColumnUpOne = world.getBlockState(new BlockPos(westEndX+i,y+1,northEndZ)).getBlock();
                    if (nextColumn == Blocks.air || (nextColumnUpOne != Blocks.air && nextColumnUpOne != Blocks.wool && nextColumnUpOne != Blocks.gold_block  && nextColumnUpOne != Blocks.redstone_block && nextColumnUpOne != Blocks.standing_sign)) break;

                    for (int j = 0; j < 200; j++) {
                        Block nextBlock = world.getBlockState(new BlockPos(westEndX+i,y,northEndZ+j)).getBlock();
                        Block nextBlockUpOne = world.getBlockState(new BlockPos(westEndX+i,y+1,northEndZ+j)).getBlock();
                        if (nextBlock == Blocks.air || (nextBlockUpOne != Blocks.air && nextBlockUpOne != Blocks.wool && nextBlockUpOne != Blocks.gold_block && nextBlockUpOne != Blocks.redstone_block && nextBlockUpOne != Blocks.standing_sign)) break;

                        blockList.add(nextBlock.toString());
                    }
                }
            } else if (southWidth(x,y,z) > northWidth(x,y,z)) {
                int southEndZ = endOfRoom(x,y,z,"s");
                int westEndX = endOfRoom(x,y,southEndZ,"w");
                for (int i = 0; i < 200; i++) {
                    Block nextColumn = world.getBlockState(new BlockPos(westEndX+i,y,southEndZ)).getBlock();
                    Block nextColumnUpOne = world.getBlockState(new BlockPos(westEndX+i,y+1,southEndZ)).getBlock();
                    if (nextColumn == Blocks.air || (nextColumnUpOne != Blocks.air && nextColumnUpOne != Blocks.wool && nextColumnUpOne != Blocks.gold_block  && nextColumnUpOne != Blocks.redstone_block && nextColumnUpOne != Blocks.standing_sign)) break;

                    for (int j = 0; j < 200; j++) {
                        Block nextBlock = world.getBlockState(new BlockPos(westEndX+i,y,southEndZ-j)).getBlock();
                        Block nextBlockUpOne = world.getBlockState(new BlockPos(westEndX+i,y+1,southEndZ-j)).getBlock();
                        if (nextBlock == Blocks.air || (nextBlockUpOne != Blocks.air && nextBlockUpOne != Blocks.wool && nextBlockUpOne != Blocks.gold_block && nextBlockUpOne != Blocks.redstone_block && nextBlockUpOne != Blocks.standing_sign)) break;

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
}
