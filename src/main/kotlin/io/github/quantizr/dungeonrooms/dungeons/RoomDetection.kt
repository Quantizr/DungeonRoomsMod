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
package io.github.quantizr.dungeonrooms.dungeons

import io.github.quantizr.dungeonrooms.ChatTransmitter
import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.dungeons.data.room.RoomColor
import io.github.quantizr.dungeonrooms.dungeons.data.room.RoomSize
import io.github.quantizr.dungeonrooms.utils.MapUtils
import io.github.quantizr.dungeonrooms.utils.RoomDetectionUtils
import io.github.quantizr.dungeonrooms.utils.Utils
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Point
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class RoomDetection {
    val mc: Minecraft = Minecraft.getMinecraft()

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!Utils.inCatacombs) return

        // From this point forward, everything assumes that Utils.inCatacombs == true
        if (DungeonRooms.instance.dungeonManager.gameStage != DungeonRunStage.RoomClear) return

        // Room clearing phase
        stage2Ticks++
        if (stage2Ticks == 10) {
            stage2Ticks = 0
            //start ExecutorService with one thread
            if (stage2Executor.isTerminated) {
                DungeonRooms.logger.info("DungeonRooms: New Single Thread Executor Started")
                stage2Executor = Executors.newSingleThreadExecutor()
            }
            //set entranceMapCorners
            if (DungeonManager.entranceMapCorners == null) {
                DungeonManager.map = MapUtils.updatedMap()
                DungeonManager.entranceMapCorners = MapUtils.entranceMapCorners(DungeonManager.map)
                DungeonRooms.logger.info("DungeonRooms: Getting entrance map corners from hotbar map...")
            } else if (DungeonManager.entranceMapCorners!![0] == null || DungeonManager.entranceMapCorners!![1] == null) { //prevent crashes if hotbar map bugged
                DungeonRooms.logger.warn("DungeonRooms: Entrance room not found, hotbar map possibly bugged")
                entranceMapNullCount++
                DungeonManager.entranceMapCorners = null // retry getting corners again next loop
                if (entranceMapNullCount == 8) {
                    ChatTransmitter.addToQueue(
                        "${EnumChatFormatting.RED}DungeonRooms: Error with hotbar map, perhaps your texture pack is interfering with room detection?"
                    )
                    DungeonRooms.textToDisplay = ArrayList(
                        listOf(
                            "Dungeon Rooms: ${EnumChatFormatting.RED}Hotbar map may be bugged"
                        )
                    )
                    //gameStage = 4;
                    //DungeonRooms.logger.info("DungeonRooms: gameStage set to " + gameStage);
                }
            } else if (DungeonManager.entrancePhysicalNWCorner == null) {
                DungeonRooms.logger.warn("DungeonRooms: Entrance Room coordinates not found")
                // for when people dc and reconnect, or if initial check doesn't work
                val playerMarkerPos = MapUtils.playerMarkerPos()
                if (playerMarkerPos != null) {
                    val closestNWMapCorner = MapUtils.getClosestNWMapCorner(
                        playerMarkerPos,
                        DungeonManager.entranceMapCorners!![0]!!,
                        DungeonManager.entranceMapCorners!![1]!!
                    )
                    if (MapUtils.getMapColor(playerMarkerPos, DungeonManager.map) == RoomColor.GREEN
                        && MapUtils.getMapColor(closestNWMapCorner, DungeonManager.map) == RoomColor.GREEN
                    ) {
                        if (mc.thePlayer.positionVector != Vec3(0.0, 0.0, 0.0)) {
                            DungeonManager.entrancePhysicalNWCorner =
                                MapUtils.getClosestNWPhysicalCorner(mc.thePlayer.positionVector)
                            DungeonRooms.logger.info("DungeonRooms: entrancePhysicalNWCorner has been set to ${DungeonManager.entrancePhysicalNWCorner}")
                        }
                    } else {
                        DungeonRooms.textToDisplay = ArrayList(
                            listOf(
                                "Dungeon Rooms: ${EnumChatFormatting.RED}Entrance Room coordinates not found",
                                "${EnumChatFormatting.RED}Please go back into the middle of the Green Entrance Room."
                            )
                        )
                    }
                }
            } else {
                val currentPhysicalCorner = MapUtils.getClosestNWPhysicalCorner(mc.thePlayer.positionVector)
                if (!currentPhysicalSegments.contains(currentPhysicalCorner)) {
                    // checks if current location is within the bounds of the last detected room
                    resetCurrentRoom() // only instance of resetting room other than leaving Dungeon
                } else if (incompleteScan != 0L && System.currentTimeMillis() > incompleteScan) {
                    incompleteScan = 0
                    DungeonRooms.logger.info("DungeonRooms: Rescanning room...")
                    raytraceBlocks()
                } else if (redoScan != 0L && System.currentTimeMillis() > redoScan) {
                    redoScan = 0
                    DungeonRooms.logger.info("DungeonRooms: Clearing data and rescanning room...")
                    thaPossibleRooms = null
                    raytraceBlocks()
                }
                if (roomSize == RoomSize.undefined || roomColor == RoomColor.UNDEFINED) {
                    updateCurrentRoom()
                    if (roomColor == RoomColor.UNDEFINED) {
                        DungeonRooms.textToDisplay = ArrayList(
                            listOf(
                                "Dungeon Rooms: ${EnumChatFormatting.RED}Waiting for hotbar map to update..."
                            )
                        )
                    } else {
                        when (roomColor) {
                            RoomColor.BROWN, RoomColor.PURPLE, RoomColor.ORANGE -> raytraceBlocks()
                            RoomColor.YELLOW -> {
                                roomName = "Miniboss Room"
                                newRoom()
                            }

                            RoomColor.GREEN -> {
                                roomName = "Entrance Room"
                                newRoom()
                            }

                            RoomColor.PINK -> {
                                roomName = "Fairy Room"
                                newRoom()
                            }

                            RoomColor.RED -> {
                                roomName = "Blood Room"
                                newRoom()
                            }

                            else -> roomName = "undefined"
                        }
                    }
                }
            }
        }

        // these run every tick while in room clearing phase
        if (futureUpdatePossibleRooms != null && futureUpdatePossibleRooms!!.isDone) {
            thaPossibleRooms = futureUpdatePossibleRooms!!.get()
            futureUpdatePossibleRooms = null
            val possibleRoomsSet = TreeSet<String>()
            var tempDirection = "undefined"
            for ((key, possibleRoomList) in thaPossibleRooms!!) {
                if (possibleRoomList.isNotEmpty()) {
                    tempDirection =
                        key // get direction to be used if room identified
                    possibleRoomsSet.addAll(possibleRoomList)
                }
            }
            when (possibleRoomsSet.size) {
                // no match
                0 -> {
                    DungeonRooms.textToDisplay = listOf(
                        "Dungeon Rooms: ${EnumChatFormatting.RED}No Matching Rooms Detected",
                        "${EnumChatFormatting.RED}This mod might not have data for this room.",
                        "${EnumChatFormatting.WHITE}Retrying every 5 seconds..."
                    )

                    redoScan = System.currentTimeMillis() + 5000
                }
                //room found
                1 -> {
                    roomName = possibleRoomsSet.first()
                    roomDirection = tempDirection
                    roomCorner = MapUtils.getPhysicalCornerPos(roomDirection, currentPhysicalSegments)
                    DungeonRooms.logger.info(
                        "DungeonRooms: 576 raytrace vectors sent, returning ${currentScannedBlocks.size} unique line-of-sight blocks, filtered down to $totalBlocksAvailableToCheck blocks, out of which ${blocksUsed.size} blocks were used to uniquely identify $roomName."
                    )
                    newRoom()
                }
                // too many matches
                else -> {
                    DungeonRooms.textToDisplay = listOf(
                        "Dungeon Rooms: ${EnumChatFormatting.RED}Unable to Determine Room Name",
                        "${EnumChatFormatting.RED}Not enough valid blocks were scanned, look at a more open area.",
                        "${EnumChatFormatting.WHITE}Retrying every second..."
                    )

                    DungeonRooms.logger.info("DungeonRooms: Possible rooms list = ${ArrayList(possibleRoomsSet)}")
                    incompleteScan = System.currentTimeMillis() + 1000
                }
            }
        }
    }

    private fun updateCurrentRoom() {
        DungeonManager.map = MapUtils.updatedMap()
        if (DungeonManager.map == null) {
            return
        }
        val currentPhysicalCorner = MapUtils.getClosestNWPhysicalCorner(mc.thePlayer.positionVector)
        val currentMapCorner = MapUtils.physicalToMapCorner(
            currentPhysicalCorner,
            DungeonManager.entrancePhysicalNWCorner!!,
            DungeonManager.entranceMapCorners!![0]!!,
            DungeonManager.entranceMapCorners!![1]!!
        )
        roomColor = MapUtils.getMapColor(currentMapCorner, DungeonManager.map)
        if (roomColor == RoomColor.UNDEFINED) {
            println("DungeonRooms: Room color is undefined")
            return
        }
        currentMapSegments = MapUtils.neighboringSegments(
            currentMapCorner,
            DungeonManager.map,
            DungeonManager.entranceMapCorners!![0]!!,
            DungeonManager.entranceMapCorners!![1]!!,
            ArrayList()
        )
        currentPhysicalSegments = ArrayList()
        for (mapCorner in currentMapSegments) {
            currentPhysicalSegments.add(
                MapUtils.mapToPhysicalCorner(
                    mapCorner,
                    DungeonManager.entrancePhysicalNWCorner!!,
                    DungeonManager.entranceMapCorners!![0]!!,
                    DungeonManager.entranceMapCorners!![1]!!
                )
            )
        }
        roomSize = MapUtils.roomSize(currentMapSegments)
        roomCategory = MapUtils.roomCategory(roomSize, roomColor)
    }

    private fun raytraceBlocks() {
        DungeonRooms.logger.info("DungeonRooms: Raytracing visible blocks")
        val timeStart = System.currentTimeMillis()

        val player = mc.thePlayer
        val eyes = Vec3(player.posX, player.posY + player.getEyeHeight().toDouble(), player.posZ)

        val blocksToCheck = HashMap<BlockPos, Int>().also { blocksToCheck ->
            // create a list of vectors to check
            RoomDetectionUtils.vectorsToRaytrace(24)
                // raytrace block
                .map { player.entityWorld.rayTraceBlocks(eyes, it, false, false, true) }

                // filter out entities/nulls
                .filter { Objects.nonNull(it) }
                .filter { it.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK }

                // get block position
                .map { it.blockPos }
                .forEach { raytracedBlockPos ->
                    if (!currentScannedBlocks.contains(raytracedBlockPos)) {
                        currentScannedBlocks.add(raytracedBlockPos)
                        // scanned block is outside of current room
                        if (currentPhysicalSegments.contains(MapUtils.getClosestNWPhysicalCorner(raytracedBlockPos))) {
                            // scanned block may be part of a corridor
                            if (!RoomDetectionUtils.blockPartOfDoorway(raytracedBlockPos)) {
                                val hitBlock = mc.theWorld.getBlockState(raytracedBlockPos)
                                val identifier =
                                    Block.getIdFromBlock(hitBlock.block) * 100 + hitBlock.block.damageDropped(hitBlock)
                                if (RoomDetectionUtils.whitelistedBlocks.contains(identifier)) {
                                    blocksToCheck[raytracedBlockPos] =
                                        identifier // will be checked and filtered in getPossibleRooms()
                                }
                            }
                        }
                    }

                }
        }

//        val vecList = RoomDetectionUtils.vectorsToRaytrace(24)
//        for (vec in vecList) {
//            // The super fancy Minecraft built-in raytracing function so that the mod only scan line of sight blocks!
//            // This is the ONLY place where this mod accesses blocks in the physical map, and they are all within FOV
//            player.entityWorld.rayTraceBlocks(
//                eyes,
//                vec,
//                false,
//                false,
//                true
//            )?.let { raytraceResult ->
//                if (raytraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                    //the following is filtering out blocks which we don't want for detection, note that these blocks are also line of sight
//                    val raytracedBlockPos = raytraceResult.blockPos
//                    if (!currentScannedBlocks.contains(raytracedBlockPos)) {
//                        currentScannedBlocks.add(raytracedBlockPos)
//                        //scanned block is outside of current room
//                        if (currentPhysicalSegments.contains(MapUtils.getClosestNWPhysicalCorner(raytracedBlockPos))) {
//                            //scanned block may be part of a corridor
//                            if (!RoomDetectionUtils.blockPartOfDoorway(raytracedBlockPos)) {
//                                val hitBlock = mc.theWorld.getBlockState(raytracedBlockPos)
//                                val identifier =
//                                    Block.getIdFromBlock(hitBlock.block) * 100 + hitBlock.block.damageDropped(hitBlock)
//                                if (RoomDetectionUtils.whitelistedBlocks.contains(identifier)) {
//                                    blocksToCheck[raytracedBlockPos] =
//                                        identifier //will be checked  and filtered in getPossibleRooms()
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        DungeonRooms.logger.info("DungeonRooms: Finished raytracing, amount of blocks to check: ${blocksToCheck.size}")
        DungeonRooms.logger.info("DungeonRooms: Time to raytrace and filter (in ms): ${System.currentTimeMillis() - timeStart}")

        if (futureUpdatePossibleRooms == null && !stage2Executor.isTerminated) {
            // start processing in new thread to avoid lag in case of complex scan
            DungeonRooms.logger.info("DungeonRooms: Initializing Room Comparison Executor")

            val timeStart = System.currentTimeMillis()
            // Load up hashmap
            val updatedPossibleRooms: HashMap<String, List<String>>?
            val possibleDirections: List<String>
            if (thaPossibleRooms != null) {
                // load info from previous scan
                DungeonRooms.logger.info("DungeonRooms: Loading possible rooms from previous room scan...")
                updatedPossibleRooms = thaPossibleRooms
                possibleDirections = ArrayList(thaPossibleRooms!!.keys)

            } else {
                DungeonRooms.logger.info("DungeonRooms: No previous possible rooms list, creating new list...")
                // no previous scans have been done, entering all possible rooms and directions
                possibleDirections = MapUtils.possibleDirections(roomSize, currentMapSegments)
                updatedPossibleRooms = HashMap()
                for (direction in possibleDirections) {
                    updatedPossibleRooms[direction] =
                        ArrayList(DungeonRooms.instance.roomDataLoader.ROOM_DATA[roomCategory]?.keys ?: emptyList())
                }
            }

            //create HashMap of the points of the corners because they will be repeatedly used for each block
            val directionCorners = HashMap<String, Point>()
            for (direction in possibleDirections) {
                val physicalCornerPos = MapUtils.getPhysicalCornerPos(direction, currentPhysicalSegments)
                if (physicalCornerPos != null) {
                    directionCorners[direction] = physicalCornerPos
                }
            }
            DungeonRooms.logger.info("DungeonRooms: directionCorners " + directionCorners.entries)
            val blocksChecked: MutableList<BlockPos> = ArrayList()
            var doubleCheckedBlocks = 0
            for ((pos, blck) in blocksToCheck) {
                DungeonRooms.logger.info("DungeonRooms: BlockPos being checked $pos")
                var combinedMatchingRooms = 0
                for (direction in possibleDirections) {
                    //get specific id for the block to compare with ".skeleton" file room data
                    val relative = MapUtils.actualToRelative(pos, direction, directionCorners[direction]!!)
                    val idToCheck = Utils.shortToLong(
                        relative.x.toShort(),
                        relative.y.toShort(),
                        relative.z.toShort(),
                        blck.toShort()
                    )
                    val matchingRooms: MutableList<String> = ArrayList()
                    // compare with each saved ".skeleton" room
                    for (roomName in updatedPossibleRooms!![direction]!!) {
                        val index = DungeonRooms.instance.roomDataLoader.ROOM_DATA[roomCategory]!![roomName]?.let {
                            Arrays.binarySearch(
                                it, idToCheck
                            )
                        }
                        if (index != null) {
                            if (index > -1) {
                                matchingRooms.add(roomName)
                            }
                        }
                    }

                    //replace updatedPossibleRooms.get(direction) with the updated matchingRooms list
                    combinedMatchingRooms += matchingRooms.size
                    updatedPossibleRooms[direction] = matchingRooms
                    DungeonRooms.logger.info("DungeonRooms: direction checked = $direction, longID = $idToCheck, relative = $relative")
                    DungeonRooms.logger.info("DungeonRooms: updatedPossibleRooms size = ${updatedPossibleRooms[direction]!!.size} for direction $direction")
                }
                blocksChecked.add(pos)

                when (combinedMatchingRooms) {
                    0 -> {
                        DungeonRooms.logger.warn("DungeonRooms: No rooms match the input blocks after checking ${blocksChecked.size} blocks, returning")
                        break
                    }

                    1 -> {
                        // scan 10 more blocks after 1 room remaining to double-check
                        if (doubleCheckedBlocks >= 10) {
                            DungeonRooms.logger.info("DungeonRooms: One room matches after checking " + blocksChecked.size + " blocks")
                            break
                        }
                        doubleCheckedBlocks++
                    }
                }
                DungeonRooms.logger.info("DungeonRooms: $combinedMatchingRooms possible rooms after checking ${blocksChecked.size} blocks")
            }

            // only print for this condition bc other conditions break to here
            if (blocksChecked.size == blocksToCheck.size) {
                DungeonRooms.logger.warn("DungeonRooms: Multiple rooms match after checking all ${blocksChecked.size} blocks")
            }
            blocksUsed.addAll(blocksChecked)

            // add blocksToCheck size to totalBlocksAvailableToCheck and clear blocksToCheck
            totalBlocksAvailableToCheck += blocksToCheck.size
            DungeonRooms.logger.info("DungeonRooms: Time to check blocks using thread (in ms): ${System.currentTimeMillis() - timeStart}")

            futureUpdatePossibleRooms = calculatePossibleRooms(blocksToCheck)
        }
    }

    private fun calculatePossibleRooms(thaBlocksToCheck: Map<BlockPos, Int>): Future<HashMap<String, List<String>>> {
        return stage2Executor.submit<HashMap<String, List<String>>?> {
            try {
                val timeStart = System.currentTimeMillis()
                // Load up hashmap
                val updatedPossibleRooms: HashMap<String, List<String>>?
                val possibleDirections: List<String>
                if (thaPossibleRooms != null) {
                    // load info from previous scan
                    DungeonRooms.logger.info("DungeonRooms: Loading possible rooms from previous room scan...")
                    updatedPossibleRooms = thaPossibleRooms
                    possibleDirections = ArrayList(thaPossibleRooms!!.keys)
                } else {
                    DungeonRooms.logger.info("DungeonRooms: No previous possible rooms list, creating new list...")
                    // no previous scans have been done, entering all possible rooms and directions
                    possibleDirections = MapUtils.possibleDirections(roomSize, currentMapSegments)
                    updatedPossibleRooms = HashMap()
                    for (direction in possibleDirections) {
                        updatedPossibleRooms[direction] =
                            ArrayList(DungeonRooms.instance.roomDataLoader.ROOM_DATA[roomCategory]?.keys ?: emptyList())
                    }
                }

                //create HashMap of the points of the corners because they will be repeatedly used for each block
                val directionCorners = HashMap<String, Point>()
                for (direction in possibleDirections) {
                    val physicalCornerPos = MapUtils.getPhysicalCornerPos(direction, currentPhysicalSegments)
                    if (physicalCornerPos != null) {
                        directionCorners[direction] = physicalCornerPos
                    }
                }
                DungeonRooms.logger.info("DungeonRooms: directionCorners ${directionCorners.entries}")
                val blocksChecked: MutableList<BlockPos> = ArrayList()
                var doubleCheckedBlocks = 0
                for ((pos, blck) in thaBlocksToCheck) {
                    DungeonRooms.logger.info("DungeonRooms: BlockPos being checked $pos")
                    var combinedMatchingRooms = 0
                    for (direction in possibleDirections) {
                        //get specific id for the block to compare with ".skeleton" file room data
                        val relative = MapUtils.actualToRelative(pos, direction, directionCorners[direction]!!)
                        val idToCheck = Utils.shortToLong(
                            relative.x.toShort(),
                            relative.y.toShort(),
                            relative.z.toShort(),
                            blck.toShort()
                        )
                        val matchingRooms: MutableList<String> = ArrayList()
                        // compare with each saved ".skeleton" room
                        for (roomName in updatedPossibleRooms!![direction]!!) {
                            val index = DungeonRooms.instance.roomDataLoader.ROOM_DATA[roomCategory]!![roomName]?.let {
                                Arrays.binarySearch(
                                    it, idToCheck
                                )
                            }
                            if (index != null) {
                                if (index > -1) {
                                    matchingRooms.add(roomName)
                                }
                            }
                        }

                        //replace updatedPossibleRooms.get(direction) with the updated matchingRooms list
                        combinedMatchingRooms += matchingRooms.size
                        updatedPossibleRooms[direction] = matchingRooms
                        DungeonRooms.logger.info("DungeonRooms: direction checked = $direction, longID = $idToCheck, relative = $relative")
                        DungeonRooms.logger.info("DungeonRooms: updatedPossibleRooms size = ${updatedPossibleRooms[direction]!!.size} for direction $direction")
                    }
                    blocksChecked.add(pos)
                    if (combinedMatchingRooms == 0) {
                        DungeonRooms.logger.warn("DungeonRooms: No rooms match the input blocks after checking ${blocksChecked.size} blocks, returning")
                        break
                    }
                    if (combinedMatchingRooms == 1) {
                        // scan 10 more blocks after 1 room remaining to double-check
                        if (doubleCheckedBlocks >= 10) {
                            DungeonRooms.logger.info("DungeonRooms: One room matches after checking ${blocksChecked.size} blocks")
                            break
                        }
                        doubleCheckedBlocks++
                    }
                    DungeonRooms.logger.info("DungeonRooms: $combinedMatchingRooms possible rooms after checking ${blocksChecked.size} blocks")
                }

                // only print for this condition bc other conditions break to here
                if (blocksChecked.size == thaBlocksToCheck.size) {
                    DungeonRooms.logger.warn("DungeonRooms: Multiple rooms match after checking all ${blocksChecked.size} blocks")
                }
                blocksUsed.addAll(blocksChecked)

                // add blocksToCheck size to totalBlocksAvailableToCheck and clear blocksToCheck
                totalBlocksAvailableToCheck += thaBlocksToCheck.size
                val timeFinish = System.currentTimeMillis()
                DungeonRooms.logger.info("DungeonRooms: Time to check blocks using thread (in ms): ${timeFinish - timeStart}")
                return@submit updatedPossibleRooms
            } catch (e: Exception) {
                DungeonRooms.logger.error("DungeonRooms: Error in Room Comparison Executor", e)
                e.printStackTrace()
                return@submit HashMap<String, List<String>>()
            }
        }
    }

    var stage2Executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var stage2Ticks = 0
    var currentMapSegments: List<Point> = ArrayList()
    var currentPhysicalSegments: MutableList<Point> = ArrayList()
    var roomSize = RoomSize.undefined
    var roomColor = RoomColor.UNDEFINED
    var roomCategory = "undefined"
    var roomName = "undefined"
    var roomDirection = "undefined"
    var roomCorner: Point? = null
    private var currentScannedBlocks = HashSet<BlockPos>()
    private var totalBlocksAvailableToCheck = 0
    var blocksUsed: MutableList<BlockPos> = ArrayList()
    private var futureUpdatePossibleRooms: Future<HashMap<String, List<String>>>? = null
    private var thaPossibleRooms: HashMap<String, List<String>>? = null
    private var incompleteScan = 0L
    private var redoScan = 0L
    var entranceMapNullCount = 0
    fun resetCurrentRoom() {
        DungeonRooms.textToDisplay = null
        Waypoints.allFound = false
        currentPhysicalSegments = emptyList<Point>().toMutableList()
        currentMapSegments = emptyList()
        roomSize = RoomSize.undefined
        roomColor = RoomColor.UNDEFINED
        roomCategory = "undefined"
        roomName = "undefined"
        roomDirection = "undefined"
        roomCorner = null
        currentScannedBlocks = HashSet()
        totalBlocksAvailableToCheck = 0
        blocksUsed = ArrayList()
        futureUpdatePossibleRooms = null
        thaPossibleRooms = null
        incompleteScan = 0
        redoScan = 0
        Waypoints.secretCount = 0
    }

    private fun newRoom() {
        if (roomName == "undefined" || roomCategory == "undefined") return
        // update Waypoints info
        val roomJson = DungeonRooms.instance.roomDataLoader.roomData[roomName]
        if (roomJson != null) {
            Waypoints.secretCount = roomJson.data.secrets
            Waypoints.allSecretsMap.putIfAbsent(roomName, ArrayList(Collections.nCopies(Waypoints.secretCount, true)))
        } else {
            Waypoints.secretCount = 0
            Waypoints.allSecretsMap.putIfAbsent(roomName, ArrayList(Collections.nCopies(0, true)))
        }

        Waypoints.secretsList = Waypoints.allSecretsMap[roomName]?.toMutableList()

        //update GUI text
        if (DRMConfig.guiToggled) {
            val lineList: MutableList<String> = ArrayList()
            val roomJson = DungeonRooms.instance.roomDataLoader.roomData[roomName]
            if (roomJson != null) {
                var line = "Dungeon Rooms: You are in ${EnumChatFormatting.GREEN}$roomCategory${EnumChatFormatting.WHITE} - ${EnumChatFormatting.GREEN}$roomName"
                if (roomJson.data.fairysoul) {
                    line += "${EnumChatFormatting.WHITE} - ${EnumChatFormatting.LIGHT_PURPLE}Fairy Soul"
                }
                lineList.add(line)
            } else if (DRMConfig.waypointsEnabled){
                lineList.add("${EnumChatFormatting.RED}No waypoints available")
                lineList.add("${EnumChatFormatting.RED}Press \"${DRMConfig.openSecretImages.display}\" to view images")
            }
            DungeonRooms.textToDisplay = lineList
        }

    }
}