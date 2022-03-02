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

package io.github.quantizr.dungeonrooms.utils;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MapUtils {

    /**
     * Checks whether the Skyblock Dungeon Map is in the player's hotbar
     * @return whether the map exists
     */
    public static boolean mapExists() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack mapSlot = mc.thePlayer.inventory.getStackInSlot(8); //check last slot where map should be
        if (mapSlot == null || mapSlot.getItem() != Items.filled_map || !mapSlot.hasDisplayName()) return false; //make sure it is a map, not SB Menu or Spirit Bow, etc
        return mapSlot.getDisplayName().contains("Magical Map");
    }

    /**
     * Reads the hotbar map and converts it into a 2D Integer array of RGB colors which can be used by the rest of the
     * code
     *
     * @return null if map not found, otherwise 128x128 Array of the RGB Integer colors of each point on the map
     */
    public static Integer[][] updatedMap() {
        if (!mapExists()) return null; //make sure map exists
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack mapSlot = mc.thePlayer.inventory.getStackInSlot(8); //get map ItemStack
        MapData mapData = Items.filled_map.getMapData(mapSlot, mc.theWorld);
        if(mapData == null) return null;
        Integer[][] map = new Integer[128][128];

        //for loop code modified from net.minecraft.client.gui.MapItemRenderer.updateMapTexture()
        for (int i = 0; i < 16384; ++i) {
            int x = i % 128; //get x coordinate of pixel being read
            int y = i / 128; //get y coordinate of pixel being read
            int j = mapData.colors[i] & 255;
            int rgba;
            if (j / 4 == 0) {
                rgba = (i + i / 128 & 1) * 8 + 16 << 24;
            } else {
                rgba = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
            }
            map[x][y] = rgba & 0x00FFFFFF; //get rgb value from rgba
        }

        return map;
    }


    /**
     * This function finds the coordinates of the NW and NE corners of the entrance room on the hotbar map. This is
     * later used to determine the size of the room grid on the hotbar map. Different floors have slightly different
     * pixel widths of the rooms, so it is important for the mod to be able to identify the location and size of various
     * portions of the room grid. Since all rooms within a floor are the same size on the hotbar map and since the
     * entrance room is always there on the hotbar map, we get two corners from the entrance room to determine the
     * scaling of the map as soon as the player enters.
     *
     * This function works by iterating through the map and looking for a green entrance room pixel. Once it finds one
     * and determines that the map pixel above is a blank spot, it checks for map pixels on the left and right side.
     *
     * @return `entranceMapCorners[0]` is the coordinate of the left NW corner and `entranceMapCorners[1]` is the
     * coordinate of the right NE corner
     */
    public static Point[] entranceMapCorners(Integer[][] map) {
        if (map == null) return null;
        Point[] corners = new Point[2];

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                if (map[x][y] != null && map[x][y] == 31744 && map[x][y-1] != null && map[x][y-1] == 0) { //check for Green entrance room pixels and make sure row above is blank
                    if (map[x - 1][y] != null && map[x - 1][y] == 0) {
                        corners[0] = new Point(x, y); //Left corner
                    } else if (map[x + 1][y] != null && map[x + 1][y] == 0) {
                        corners[1] = new Point(x, y); //Right Corner
                    }
                }
            }
            if (corners[0] != null && corners[1] != null) break;
        }
        return corners;
    }

    /**
     * @return the coordinate of the NW hotbar map corner closest to the coordinate provided
     */
    public static Point getClosestNWMapCorner(Point mapPos, Point leftCorner, Point rightCorner) {
        int roomWidthAndGap = rightCorner.x - leftCorner.x + 1 + 4; //+1 to count left corner block, +4 to account for gap between rooms
        Point origin = new Point(leftCorner.x % roomWidthAndGap, leftCorner.y % roomWidthAndGap);

        mapPos.x = mapPos.x + 2; //shift by 2 so room borders are evenly split
        mapPos.y = mapPos.y + 2;

        int x = mapPos.x - (mapPos.x % roomWidthAndGap) + origin.x; //round down to room size grid
        int y = mapPos.y - (mapPos.y % roomWidthAndGap) + origin.y;

        if (x > mapPos.x) x -= roomWidthAndGap; //make sure coordinates are being rounded down (in case origin value being too large)
        if (y > mapPos.y) y -= roomWidthAndGap;

        return new Point(x, y);
    }

    /**
     * Skyblock Dungeon maps are aligned to a 32x32 block wide grid, allowing for math only calculation of corners
     * @return the coordinate of the NW physical map corner closest to the coordinate provided
     */
    public static Point getClosestNWPhysicalCorner (Vec3 vectorPos) {
        Vec3 shiftedPos = vectorPos.addVector(0.5, 0, 0.5); //shift by 0.5 so room borders are evenly split
        shiftedPos = shiftedPos.addVector(8, 0, 8); //because Hypixel randomly shifted rooms in Skyblock 0.12.3
        int x = (int) (shiftedPos.xCoord - Math.floorMod((int) shiftedPos.xCoord, 32)); //room length 31, +1 to account for gap between rooms
        int z = (int) (shiftedPos.zCoord - Math.floorMod((int) shiftedPos.zCoord, 32));

        return new Point(x - 8 , z - 8); //-8 for same reason as above
    }

    public static Point getClosestNWPhysicalCorner (BlockPos blockPos) {
        return getClosestNWPhysicalCorner(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    /**
     * @return the hotbar map coordinate corresponding to the physical map corner coordinate provided
     */
    public static Point physicalToMapCorner(Point physicalClosestCorner, Point physicalLeftCorner, Point leftCorner, Point rightCorner) {
        int roomWidthAndGap = rightCorner.x - leftCorner.x + 1 + 4; //+1 to count left corner block, +4 to account for gap between rooms
        int xShift = (physicalClosestCorner.x - physicalLeftCorner.x) / 32; //how many 1x1 grids away the closest corner is from entrance
        int yShift = (physicalClosestCorner.y - physicalLeftCorner.y) / 32;

        int x = leftCorner.x + (roomWidthAndGap * xShift); //translate grid distance to the map
        int y = leftCorner.y + (roomWidthAndGap * yShift);

        //this function can return a value outside of 0-127 which are the bounds of the map
        //must check bounds for any function using this

        return new Point(x, y);
    }

    /**
     * @return the physical map coordinate corresponding to the hotbar map corner coordinate provided
     */
    public static Point mapToPhysicalCorner(Point mapCorner, Point physicalLeftCorner, Point leftCorner, Point rightCorner) {
        int roomWidthAndGap = rightCorner.x - leftCorner.x + 1 + 4; //+1 to count left corner block, +4 to account for gap between rooms
        int xShift = (mapCorner.x - leftCorner.x) / roomWidthAndGap; //how many 1x1 grids away the closest corner is from entrance
        int yShift = (mapCorner.y - leftCorner.y) / roomWidthAndGap;

        int x = physicalLeftCorner.x + (32 * xShift); //translate grid distance to the physical
        int y = physicalLeftCorner.y + (32 * yShift);

        return new Point(x, y);
    }

    /**
     * @return the color of the coordinate on the map as a String
     */
    public static String getMapColor(Point point, Integer[][] map) {
        int x = point.x;
        int y = point.y;

        //physicalToMapCorner might be called when player is outside map zone e.g. boss room, returning a value outside
        //of the map coordinates which would otherwise call getMapColor to crash
        if (x < 0 || y < 0 || x > 127 || y > 127) {
            return "undefined";
        }

        if (map != null) {
            switch (map[x][y]) {
                case 7488283:
                    return "brown";
                case 11685080:
                    return "purple";
                case 15066419:
                    return "yellow";
                case 31744:
                    return "green";
                case 15892389:
                    return "pink";
                case 14188339:
                    return "orange";
                case 16711680:
                    return "red";
                default: //includes default blank background color = 0 and any other possible map colors
                    return "undefined";
            }
        }
        return "undefined";
    }

    /**
     * Checks a point on each side the room corner coordinate for either empty space or a connected segment, each new
     * segment is recursively checked until all neighboring segments have been added to a list
     *
     * @return a List of the coordinates of all segments of the same room as the corner coordinate provided
     */
    public static List<Point> neighboringSegments(Point originCorner, Integer[][] map, Point leftCorner, Point rightCorner, List<Point> list) {
        if (!list.contains(originCorner)) {
            list.add(originCorner);
        }
        if (!getMapColor(originCorner, map).equals("brown")) return list; //only continue if square is brown

        int roomWidth = rightCorner.x - leftCorner.x + 1; //+1 to count left corner block


        List<Point> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(new Point(originCorner.x, originCorner.y - 1)); //up
        pointsToCheck.add(new Point(originCorner.x, originCorner.y + roomWidth)); //down
        pointsToCheck.add(new Point(originCorner.x - 1, originCorner.y)); //left
        pointsToCheck.add(new Point(originCorner.x + roomWidth, originCorner.y)); //right

        List<Point> pointsToTransform = new ArrayList<>(); //pointsToCheck +/- 4 to jump gap and calc correct closest NW corner
        pointsToTransform.add(new Point(originCorner.x, originCorner.y - 1 - 4)); //up
        pointsToTransform.add(new Point(originCorner.x, originCorner.y + roomWidth + 4)); //down
        pointsToTransform.add(new Point(originCorner.x -1 - 4, originCorner.y)); //left
        pointsToTransform.add(new Point(originCorner.x + roomWidth + 4, originCorner.y)); //right

        for (int i = 0; i < 4; i++) {
            if (getMapColor(pointsToCheck.get(i), map).equals("brown")) {
                Point newCorner = getClosestNWMapCorner(pointsToTransform.get(i), leftCorner, rightCorner);

                if (!list.contains(newCorner)) {
                    list.add(newCorner);
                    list = neighboringSegments(newCorner, map, leftCorner, rightCorner, list); //check for neighboring segments from the new point
                }
            }
        }

        return list;
    }

    /**
     * @return the size of the room given a the location of each room segment
     */
    public static String roomSize(List<Point> segments) {
        //accepts either map segments or physical segments, does not matter
        if (segments.size() == 1) return "1x1";
        if (segments.size() == 2) return "1x2";

        HashSet<Integer> x = new HashSet<>();
        HashSet<Integer> y = new HashSet<>();
        for(Point segment:segments) {
            x.add(segment.x);
            y.add(segment.y);
        }
        if (segments.size() == 3) {
            if (x.size() == 2 && y.size() == 2) return "L-shape";
            else return "1x3";
        }
        if (segments.size() == 4) {
            if (x.size() == 2 && y.size() == 2) return "2x2";
            else return "1x4";
        }

        return "undefined";
    }

    /**
     * @return the category of the room given size and color
     */
    public static String roomCategory(String roomSize, String roomColor) {
        if (roomSize.equals("1x1")) {
            switch (roomColor) {
                case "brown":
                    return "1x1";
                case "purple":
                    return "Puzzle";
                case "orange":
                    return "Trap";
                case "green":
                case "red":
                case "pink":
                case "yellow":
                    return "General";
                default:
                    return "undefined";
            }
        } else {
            return roomSize;
        }
    }

    /**
     * @return the coordinate of the player marker on the map
     */
    public static Point playerMarkerPos() {
        if (!mapExists()) return null; //make sure map exists
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack mapSlot = mc.thePlayer.inventory.getStackInSlot(8); //get map ItemStack
        MapData mapData = Items.filled_map.getMapData(mapSlot, mc.theWorld);
        if(mapData == null) return null;
        if (mapData.mapDecorations != null) {
            for (Map.Entry<String, Vec4b> entry : mapData.mapDecorations.entrySet()) {
                if (entry.getValue().func_176110_a() == 1) { //player marker
                    int x = entry.getValue().func_176112_b() / 2 + 64;
                    int y = entry.getValue().func_176113_c() / 2 + 64;
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * @return the location of the furthest corner in a direction given a list of segments
     */
    public static Point getPhysicalCornerPos(String direction, List<Point> currentPhysicalSegments) {
        //For L-shapes, this will return the fourth corner as if it were a 2x2
        TreeSet<Integer> xSet = new TreeSet<>(); //TreeSet removes duplicates and sorts increasing
        TreeSet<Integer> ySet = new TreeSet<>();
        for(Point segment:currentPhysicalSegments) {
            xSet.add(segment.x);
            ySet.add(segment.y);
        }

        switch (direction) {
            case "NW":
                return new Point(xSet.first(), ySet.first());
            case "NE":
                return new Point(xSet.last() + 30, ySet.first());
            case "SE":
                return new Point(xSet.last() + 30, ySet.last() + 30);
            case "SW":
                return new Point(xSet.first(), ySet.last() + 30);
        }
        return null;
    }

    /**
     * Rooms such as L-shape rooms only have one possible direction, so it is much faster to only have to compare one
     * direction instead of rotating it 4 times. Similarly, rectangular shaped rooms have two possible directions,
     * saving time instead of rotating it 4 times. Square shaped rooms must be checked in all four directions.
     *
     * @return the possible rotation directions which need to be checked
     */
    public static List<String> possibleDirections(String roomSize, List<Point> currentRoomSegments) {
        //can take physical or hotbar segments
        //eliminates two possibilities for rectangular rooms, three possibilities for L-shape
        List<String> directions = new ArrayList<>();
        if (roomSize.equals("1x1") || roomSize.equals("2x2")) {
            directions.add("NW");
            directions.add("NE");
            directions.add("SE");
            directions.add("SW");
        } else {
            TreeSet<Integer> xSet = new TreeSet<>(); //TreeSet removes duplicates and sorts increasing
            TreeSet<Integer> ySet = new TreeSet<>();
            for(Point segment:currentRoomSegments) {
                xSet.add(segment.x);
                ySet.add(segment.y);
            }
            if (roomSize.equals("L-shape")) {
                List<Integer> x = new ArrayList<>(xSet);
                List<Integer> y = new ArrayList<>(ySet);

                if (!currentRoomSegments.contains(new Point(x.get(0), y.get(0)))) directions.add("SW");
                else if (!currentRoomSegments.contains(new Point(x.get(0), y.get(1)))) directions.add("SE");
                else if (!currentRoomSegments.contains(new Point(x.get(1), y.get(0)))) directions.add("NW");
                else if (!currentRoomSegments.contains(new Point(x.get(1), y.get(1)))) directions.add("NE");

            } else if (roomSize.startsWith("1x")) { //not 1x1 bc else statement earlier
                if (xSet.size() >= 2  && ySet.size() == 1) {
                    directions.add("NW");
                    directions.add("SE");
                } else if (xSet.size() == 1  && ySet.size() >= 2) {
                    directions.add("NE");
                    directions.add("SW");
                }
            }
        }
        return directions;
    }

    /**
     * @return the actual coordinate of a block given the relative coordinate
     */
    public static BlockPos actualToRelative(BlockPos actual, String cornerDirection, Point locationOfCorner) {
        double x = 0;
        double z = 0;
        switch (cornerDirection) {
            case "NW":
                x = actual.getX() - locationOfCorner.getX();
                z = actual.getZ() - locationOfCorner.getY(); //.getY in a point is the MC Z coord
                break;
            case "NE":
                x = actual.getZ() - locationOfCorner.getY();
                z = -(actual.getX() - locationOfCorner.getX());
                break;
            case "SE":
                x = -(actual.getX() - locationOfCorner.getX());
                z = -(actual.getZ() - locationOfCorner.getY());
                break;
            case "SW":
                x = -(actual.getZ() - locationOfCorner.getY());
                z = actual.getX() - locationOfCorner.getX();
                break;
        }
        return new BlockPos(x, actual.getY(), z);
    }

    /**
     * @return the relative coordinate of a block given the actual coordinate
     */
    public static BlockPos relativeToActual(BlockPos relative, String cornerDirection, Point locationOfCorner) {
        double x = 0;
        double z = 0;
        switch (cornerDirection) {
            case "NW":
                x = relative.getX() + locationOfCorner.getX();
                z = relative.getZ() + locationOfCorner.getY(); //.getY in a point is the MC Z coord
                break;
            case "NE":
                x = -(relative.getZ() - locationOfCorner.getX());
                z = relative.getX() + locationOfCorner.getY();
                break;
            case "SE":
                x = -(relative.getX() - locationOfCorner.getX());
                z = -(relative.getZ() - locationOfCorner.getY());
                break;
            case "SW":
                x = relative.getZ() + locationOfCorner.getX();
                z = -(relative.getX() - locationOfCorner.getY());
                break;
        }
        return new BlockPos(x, relative.getY(), z);
    }
}
