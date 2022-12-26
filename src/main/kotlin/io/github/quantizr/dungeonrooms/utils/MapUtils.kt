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
package io.github.quantizr.dungeonrooms.utils

import io.github.quantizr.dungeonrooms.roomdata.RoomColor
import io.github.quantizr.dungeonrooms.roomdata.RoomSize
import net.minecraft.block.material.MapColor
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Point
import java.util.*

object MapUtils {
    /**
     * Checks whether the Skyblock Dungeon Map is in the player's hotbar
     * @return whether the map exists
     */
    fun mapExists(): Boolean {
        val mc = Minecraft.getMinecraft()
        val mapSlot = mc.thePlayer.inventory.getStackInSlot(8) //check last slot where map should be
        return if (mapSlot == null || mapSlot.item !== Items.filled_map || !mapSlot.hasDisplayName()) false else mapSlot.displayName.contains(
            "Magical Map"
        ) //make sure it is a map, not SB Menu or Spirit Bow, etc
    }

    /**
     * Reads the hotbar map and converts it into a 2D Integer array of RGB colors which can be used by the rest of the
     * code
     *
     * @return null if map not found, otherwise 128x128 Array of the RGB Integer colors of each point on the map
     */
    fun updatedMap(): Array<Array<Int?>>? {
        if (!mapExists()) return null //make sure map exists
        val mc = Minecraft.getMinecraft()
        val mapSlot = mc.thePlayer.inventory.getStackInSlot(8) //get map ItemStack
        val mapData = Items.filled_map.getMapData(mapSlot, mc.theWorld) ?: return null
        val map = Array(128) { arrayOfNulls<Int>(128) }

        //for loop code modified from net.minecraft.client.gui.MapItemRenderer.updateMapTexture()
        for (i in 0..16383) {
            val x = i % 128 //get x coordinate of pixel being read
            val y = i / 128 //get y coordinate of pixel being read
            val j = mapData.colors[i].toInt() and 255
            var rgba: Int
            rgba = if (j / 4 == 0) {
                (i + i / 128 and 1) * 8 + 16 shl 24
            } else {
                MapColor.mapColorArray[j / 4].getMapColor(j and 3)
            }
            map[x][y] = rgba and 0x00FFFFFF //get rgb value from rgba
        }
        return map
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
    fun entranceMapCorners(map: Array<Array<Int?>>?): Array<Point?>? {
        if (map == null) return null
        val corners = arrayOfNulls<Point>(2)
        for (x in 0..127) {
            for (y in 0..127) {
                if (map[x][y] != null && map[x][y] == 31744 && map[x][y - 1] != null && map[x][y - 1] == 0) { //check for Green entrance room pixels and make sure row above is blank
                    if (map[x - 1][y] != null && map[x - 1][y] == 0) {
                        corners[0] = Point(x, y) //Left corner
                    } else if (map[x + 1][y] != null && map[x + 1][y] == 0) {
                        corners[1] = Point(x, y) //Right Corner
                    }
                }
            }
            if (corners[0] != null && corners[1] != null) break
        }
        return corners
    }

    /**
     * @return the coordinate of the NW hotbar map corner closest to the coordinate provided
     */
    fun getClosestNWMapCorner(mapPos: Point, leftCorner: Point, rightCorner: Point): Point {
        val roomWidthAndGap =
            rightCorner.x - leftCorner.x + 1 + 4 //+1 to count left corner block, +4 to account for gap between rooms
        val origin = Point(leftCorner.x % roomWidthAndGap, leftCorner.y % roomWidthAndGap)
        mapPos.x = mapPos.x + 2 //shift by 2 so room borders are evenly split
        mapPos.y = mapPos.y + 2
        var x = mapPos.x - mapPos.x % roomWidthAndGap + origin.x //round down to room size grid
        var y = mapPos.y - mapPos.y % roomWidthAndGap + origin.y
        if (x > mapPos.x) x -= roomWidthAndGap //make sure coordinates are being rounded down (in case origin value being too large)
        if (y > mapPos.y) y -= roomWidthAndGap
        return Point(x, y)
    }

    /**
     * Skyblock Dungeon maps are aligned to a 32x32 block wide grid, allowing for math only calculation of corners
     * @return the coordinate of the NW physical map corner closest to the coordinate provided
     */
    fun getClosestNWPhysicalCorner(vectorPos: Vec3): Point {
        var shiftedPos = vectorPos.addVector(0.5, 0.0, 0.5) //shift by 0.5 so room borders are evenly split
        shiftedPos = shiftedPos.addVector(8.0, 0.0, 8.0) //because Hypixel randomly shifted rooms in Skyblock 0.12.3
        val x = (shiftedPos.xCoord - Math.floorMod(
            shiftedPos.xCoord.toInt(),
            32
        )).toInt() //room length 31, +1 to account for gap between rooms
        val z = (shiftedPos.zCoord - Math.floorMod(shiftedPos.zCoord.toInt(), 32)).toInt()
        return Point(x - 8, z - 8) //-8 for same reason as above
    }

    fun getClosestNWPhysicalCorner(blockPos: BlockPos): Point {
        return getClosestNWPhysicalCorner(Vec3(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble()))
    }

    /**
     * @return the hotbar map coordinate corresponding to the physical map corner coordinate provided
     */
    fun physicalToMapCorner(
        physicalClosestCorner: Point,
        physicalLeftCorner: Point,
        leftCorner: Point,
        rightCorner: Point
    ): Point {
        val roomWidthAndGap =
            rightCorner.x - leftCorner.x + 1 + 4 //+1 to count left corner block, +4 to account for gap between rooms
        val xShift =
            (physicalClosestCorner.x - physicalLeftCorner.x) / 32 //how many 1x1 grids away the closest corner is from entrance
        val yShift = (physicalClosestCorner.y - physicalLeftCorner.y) / 32
        val x = leftCorner.x + roomWidthAndGap * xShift //translate grid distance to the map
        val y = leftCorner.y + roomWidthAndGap * yShift

        //this function can return a value outside of 0-127 which are the bounds of the map
        //must check bounds for any function using this
        return Point(x, y)
    }

    /**
     * @return the physical map coordinate corresponding to the hotbar map corner coordinate provided
     */
    fun mapToPhysicalCorner(mapCorner: Point, physicalLeftCorner: Point, leftCorner: Point, rightCorner: Point): Point {
        val roomWidthAndGap =
            rightCorner.x - leftCorner.x + 1 + 4 //+1 to count left corner block, +4 to account for gap between rooms
        val xShift =
            (mapCorner.x - leftCorner.x) / roomWidthAndGap //how many 1x1 grids away the closest corner is from entrance
        val yShift = (mapCorner.y - leftCorner.y) / roomWidthAndGap
        val x = physicalLeftCorner.x + 32 * xShift //translate grid distance to the physical
        val y = physicalLeftCorner.y + 32 * yShift
        return Point(x, y)
    }

    /**
     * @return the color of the coordinate on the map as a String
     */
    fun getMapColor(point: Point, map: Array<Array<Int?>>?): RoomColor {
        val x = point.x
        val y = point.y

        //physicalToMapCorner might be called when player is outside map zone e.g. boss room, returning a value outside
        //  the map coordinates which would otherwise call getMapColor to crash
        if (x < 0 || y < 0 || x > 127 || y > 127) {
            return RoomColor.UNDEFINED
        }
        return if (map != null) {
            when (map[x][y]) {
                7488283 -> RoomColor.BROWN
                11685080 -> RoomColor.PURPLE
                15066419 -> RoomColor.YELLOW
                31744 -> RoomColor.GREEN
                15892389 -> RoomColor.PINK
                14188339 -> RoomColor.ORANGE
                16711680 -> RoomColor.RED
                else -> RoomColor.UNDEFINED
            }
        } else RoomColor.UNDEFINED
    }

    /**
     * Checks a point on each side the room corner coordinate for either empty space or a connected segment, each new
     * segment is recursively checked until all neighboring segments have been added to a list
     *
     * @return a List of the coordinates of all segments of the same room as the corner coordinate provided
     */
    fun neighboringSegments(
        originCorner: Point,
        map: Array<Array<Int?>>?,
        leftCorner: Point,
        rightCorner: Point,
        list: MutableList<Point>
    ): MutableList<Point> {
        var list = list
        if (!list.contains(originCorner)) {
            list.add(originCorner)
        }
        if (getMapColor(originCorner, map) != RoomColor.BROWN) return list //only continue if square is brown
        val roomWidth = rightCorner.x - leftCorner.x + 1 //+1 to count left corner block
        val pointsToCheck: MutableList<Point> = ArrayList()
        pointsToCheck.add(Point(originCorner.x, originCorner.y - 1)) //up
        pointsToCheck.add(Point(originCorner.x, originCorner.y + roomWidth)) //down
        pointsToCheck.add(Point(originCorner.x - 1, originCorner.y)) //left
        pointsToCheck.add(Point(originCorner.x + roomWidth, originCorner.y)) //right
        val pointsToTransform: MutableList<Point> =
            ArrayList() //pointsToCheck +/- 4 to jump gap and calc correct closest NW corner
        pointsToTransform.add(Point(originCorner.x, originCorner.y - 1 - 4)) //up
        pointsToTransform.add(Point(originCorner.x, originCorner.y + roomWidth + 4)) //down
        pointsToTransform.add(Point(originCorner.x - 1 - 4, originCorner.y)) //left
        pointsToTransform.add(Point(originCorner.x + roomWidth + 4, originCorner.y)) //right
        for (i in 0..3) {
            if (getMapColor(pointsToCheck[i], map) == RoomColor.BROWN) {
                val newCorner = getClosestNWMapCorner(pointsToTransform[i], leftCorner, rightCorner)
                if (!list.contains(newCorner)) {
                    list.add(newCorner)
                    list = neighboringSegments(
                        newCorner,
                        map,
                        leftCorner,
                        rightCorner,
                        list
                    ) //check for neighboring segments from the new point
                }
            }
        }
        return list
    }

    /**
     * @return the size of the room given a location of each room segment
     */
    fun roomSize(segments: List<Point>): RoomSize {
        //accepts either map segments or physical segments, does not matter
        if (segments.size == 1) return RoomSize.`1x1`
        if (segments.size == 2) return RoomSize.`1x2`
        val x = HashSet<Int>()
        val y = HashSet<Int>()
        for (segment in segments) {
            x.add(segment.x)
            y.add(segment.y)
        }
        if (segments.size == 3) {
            return if (x.size == 2 && y.size == 2) RoomSize.`L-shape` else RoomSize.`1x3`
        }
        return if (segments.size == 4) {
            if (x.size == 2 && y.size == 2) RoomSize.`2x2` else RoomSize.`1x4`
        } else RoomSize.undefined
    }

    /**
     * @return the category of the room given size and color
     */
    fun roomCategory(roomSize: RoomSize, roomColor: RoomColor): String {
        return if (roomSize == RoomSize.`1x1`) {
            when (roomColor) {
                RoomColor.BROWN -> "1x1"
                RoomColor.PURPLE -> "Puzzle"
                RoomColor.ORANGE -> "Trap"
                RoomColor.GREEN, RoomColor.RED, RoomColor.PINK, RoomColor.YELLOW -> "General"
                else -> "undefined"
            }
        } else {
            roomSize.toString()
        }
    }

    /**
     * @return the coordinate of the player marker on the map
     */
    fun playerMarkerPos(): Point? {
        if (!mapExists()) return null // make sure map exists
        val mc = Minecraft.getMinecraft()
        val mapSlot = mc.thePlayer.inventory.getStackInSlot(8) // get map ItemStack
        val mapData = Items.filled_map.getMapData(mapSlot, mc.theWorld) ?: return null
        if (mapData.mapDecorations != null) {
            for ((_, value) in mapData.mapDecorations) {
                if (value.func_176110_a().toInt() == 1) { // player marker
                    val x = value.func_176112_b() / 2 + 64
                    val y = value.func_176113_c() / 2 + 64
                    return Point(x, y)
                }
            }
        }
        return null
    }

    /**
     * @return the location of the furthest corner in a direction given a list of segments
     */
    fun getPhysicalCornerPos(direction: String?, currentPhysicalSegments: List<Point>): Point? {
        // For L-shapes, this will return the fourth corner as if it were a 2x2
        val xSet = TreeSet<Int>() // TreeSet removes duplicates and sorts increasing
        val ySet = TreeSet<Int>()
        for (segment in currentPhysicalSegments) {
            xSet.add(segment.x)
            ySet.add(segment.y)
        }
        when (direction) {
            "NW" -> return Point(xSet.first(), ySet.first())
            "NE" -> return Point(xSet.last() + 30, ySet.first())
            "SE" -> return Point(xSet.last() + 30, ySet.last() + 30)
            "SW" -> return Point(xSet.first(), ySet.last() + 30)
        }
        return null
    }

    /**
     * Rooms such as L-shape rooms only have one possible direction, so it is much faster to only have to compare one
     * direction instead of rotating it 4 times. Similarly, rectangular shaped rooms have two possible directions,
     * saving time instead of rotating it 4 times. Square shaped rooms must be checked in all four directions.
     *
     * @return the possible rotation directions which need to be checked
     */
    fun possibleDirections(roomSize: RoomSize, currentRoomSegments: List<Point>): List<String> {
        //can take physical or hotbar segments
        //eliminates two possibilities for rectangular rooms, three possibilities for L-shape
        val directions: MutableList<String> = ArrayList()
        if (roomSize == RoomSize.`1x1` || roomSize == RoomSize.`2x2`) {
            directions.add("NW")
            directions.add("NE")
            directions.add("SE")
            directions.add("SW")
        } else {
            val xSet = TreeSet<Int>() //TreeSet removes duplicates and sorts increasing
            val ySet = TreeSet<Int>()
            for (segment in currentRoomSegments) {
                xSet.add(segment.x)
                ySet.add(segment.y)
            }
            if (roomSize == RoomSize.`L-shape`) {
                val x: List<Int> = ArrayList(xSet)
                val y: List<Int> = ArrayList(ySet)
                if (!currentRoomSegments.contains(
                        Point(
                            x[0],
                            y[0]
                        )
                    )
                ) directions.add("SW") else if (!currentRoomSegments.contains(
                        Point(
                            x[0], y[1]
                        )
                    )
                ) directions.add("SE") else if (!currentRoomSegments.contains(
                        Point(
                            x[1],
                            y[0]
                        )
                    )
                ) directions.add("NW") else if (!currentRoomSegments.contains(
                        Point(
                            x[1], y[1]
                        )
                    )
                ) directions.add("NE")
            } else if (roomSize.toString().startsWith("1x")) { //not 1x1 bc else statement earlier
                if (xSet.size >= 2 && ySet.size == 1) {
                    directions.add("NW")
                    directions.add("SE")
                } else if (xSet.size == 1 && ySet.size >= 2) {
                    directions.add("NE")
                    directions.add("SW")
                }
            }
        }
        return directions
    }

    /**
     * @return the actual coordinate of a block given the relative coordinate
     */
    fun actualToRelative(actual: BlockPos, cornerDirection: String?, locationOfCorner: Point): BlockPos {
        var x = 0.0
        var z = 0.0
        when (cornerDirection) {
            "NW" -> {
                x = actual.x - locationOfCorner.getX()
                z = actual.z - locationOfCorner.getY() //.getY in a point is the MC Z coord
            }

            "NE" -> {
                x = actual.z - locationOfCorner.getY()
                z = -(actual.x - locationOfCorner.getX())
            }

            "SE" -> {
                x = -(actual.x - locationOfCorner.getX())
                z = -(actual.z - locationOfCorner.getY())
            }

            "SW" -> {
                x = -(actual.z - locationOfCorner.getY())
                z = actual.x - locationOfCorner.getX()
            }
        }
        return BlockPos(x, actual.y.toDouble(), z)
    }

    /**
     * @return the relative coordinate of a block given the actual coordinate
     */
    fun relativeToActual(relative: BlockPos, cornerDirection: String?, locationOfCorner: Point): BlockPos {
        var x = 0.0
        var z = 0.0
        when (cornerDirection) {
            "NW" -> {
                x = relative.x + locationOfCorner.getX()
                z = relative.z + locationOfCorner.getY() //.getY in a point is the MC Z coord
            }

            "NE" -> {
                x = -(relative.z - locationOfCorner.getX())
                z = relative.x + locationOfCorner.getY()
            }

            "SE" -> {
                x = -(relative.x - locationOfCorner.getX())
                z = -(relative.z - locationOfCorner.getY())
            }

            "SW" -> {
                x = relative.z + locationOfCorner.getX()
                z = -(relative.x - locationOfCorner.getY())
            }
        }
        return BlockPos(x, relative.y.toDouble(), z)
    }
}