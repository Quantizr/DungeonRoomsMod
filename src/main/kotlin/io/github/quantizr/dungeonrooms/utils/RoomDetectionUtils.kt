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

import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.tan

object RoomDetectionUtils {
    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    /**
     * This is a variation of Minecraft's Entity.getVectorForRotation but Minecraft and I seem to interpret yaw and
     * pitch differently
     *
     * @return vector corresponding to a rotation
     */
    private fun getVectorFromRotation(yaw: Float, pitch: Float): Vec3 {
        val f = MathHelper.cos(-yaw * DEG_TO_RAD.toFloat() - Math.PI.toFloat())
        val f1 = MathHelper.sin(-yaw * DEG_TO_RAD.toFloat() - Math.PI.toFloat())
        val f2 = -MathHelper.cos(-pitch * DEG_TO_RAD.toFloat())
        val f3 = MathHelper.sin(-pitch * DEG_TO_RAD.toFloat())
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }

    /**
     * This function takes an input vectorQuantity, and returns vectorQuantity^2 vectors which are within the player's
     * FOV. The function takes into account the FOV in the game settings, dynamic FOV changes from sprinting, speed, or
     * slowness, along with changes in perspective such as reverse F5. It is only approximates FOV stretching because of
     * the weird way Minecraft does FOV stretching, but is accurate for all aspect ratios less extreme than 8:1 and 1:8.
     * (if you're playing at an aspect ratio more extreme than 8:1 or 1:8, get help)
     *
     * @return a List of vectors which are within the player's FOV
     */
    fun vectorsToRaytrace(vectorQuantity: Int): List<Vec3> {
        //real # of vectors is vectorQuantity^2
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer
        val vectorList: MutableList<Vec3> = ArrayList()
        //get vector location of player's eyes
        val eyes = Vec3(player.posX, player.posY + player.getEyeHeight().toDouble(), player.posZ)
        val aspectRatio = mc.displayWidth.toFloat() / mc.displayHeight.toFloat()

        //Vertical FOV: Minecraft FOV setting multiplied by FOV modifier (sprinting, speed effect, etc)
        val fovV = (mc.gameSettings.fovSetting * mc.thePlayer.fovModifier).toDouble()
        //Horizontal FOV: Thanks Minecraft for being weird and making it this complicated to calculate
        val fovH = atan(aspectRatio * tan(fovV * DEG_TO_RAD / 2)) * 2 * RAD_TO_DEG
        val verticalSpacing = (fovV * 0.8 / vectorQuantity).toFloat() // * 0.8 to leave some boundary space
        val horizontalSpacing = (fovH * 0.9 / vectorQuantity).toFloat() // * 0.9 to leave some boundary space
        var playerYaw = player.rotationYaw
        var playerPitch = player.rotationPitch
        if (mc.gameSettings.thirdPersonView == 2) {
            //dumb but easy method of modifying vector direction if player is in reverse 3rd person
            //does not account for the increased 3rd person FOV, but all vectors are within player view so who cares
            playerYaw += 180.0f
            playerPitch = -playerPitch
        }
        var h = -(vectorQuantity - 1).toFloat() / 2
        while (h <= (vectorQuantity - 1).toFloat() / 2) {
            var v = -(vectorQuantity - 1).toFloat() / 2
            while (v <= (vectorQuantity - 1).toFloat() / 2) {
                val yaw = h * horizontalSpacing
                val pitch = v * verticalSpacing

                /*
                yaw and pitch are evenly spread out, but yaw needs to be scaled because MC FOV stretching weird.
                "* ((playerPitch*playerPitch/8100)+1)" because yaw otherwise doesn't get complete scan at higher pitches.
                "/ (Math.abs(v/(vectorQuantity))+1)" because Higher FOVs do not stretch out the corners of the screen as
                much as the rest of the screen, which would otherwise cause corner vectors to be outside FOV
                */
                val yawScaled = yaw * (playerPitch * playerPitch / 8100 + 1) / (abs(v / vectorQuantity) + 1)

                //turn rotation into vector
                val direction = getVectorFromRotation(yawScaled + playerYaw, pitch + playerPitch)

                //add the new direction vector * 64 (meaning when the vector is raytraced, it will return the first
                // block up to 64 blocks away) to the eyes vector to create the vector which will be raytraced
                vectorList.add(eyes.addVector(direction.xCoord * 64, direction.yCoord * 64, direction.zCoord * 64))
                v++
            }
            h++
        }
        return vectorList
    }

    /**
     * list of whitelisted blocks which the raytraced blocks will be filtered with
     */
    var whitelistedBlocks = HashSet(
        mutableListOf( //These are the blocks which are stored in the ".skeleton" files
            100,  //Stone
            103,  //Diorite
            104,  //Polished Diorite
            105,  //Andesite
            106,  //Polished Andesite
            200,  //Grass
            300,  //Dirt
            301,  //Coarse Dirt
            400,  //Cobblestone
            700,  //Bedrock
            1800,  //Oak Leaves
            3507,  //Gray Wool
            4300,  //Double Stone Slab
            4800,  //Mossy Cobblestone
            8200,  //Clay
            9800,  //Stone Bricks
            9801,  //Mossy Stone Bricks
            9803,  //Chiseled Stone Bricks
            15907,  //Gray Stained Clay
            15909,  //Cyan Stained Clay
            15915 //Black Stained Clay
        )
    )

    /**
     * Checks if a block is part of a doorway (assuming the room is a 1x1), meaning that for larger rooms such as 2x2s,
     * parts of the middle of the room will not be used for room detection.
     * The relative location of doorways is always the same so this can be checked using math alone.
     *
     * @return whether a block would be part of a doorway.
     */
    fun blockPartOfDoorway(blockToCheck: BlockPos): Boolean {
        //will also return true for blocks in the middle of rooms where there would be a doorway if it were a 1x1
        if (blockToCheck.y < 66 || blockToCheck.y > 73) return false
        val relX = Math.floorMod(blockToCheck.x - 8, 32)
        val relZ = Math.floorMod(blockToCheck.z - 8, 32)
        if (relX in 13..17) {
            if (relZ <= 2) return true
            if (relZ >= 28) return true
        }
        if (relZ in 13..17) {
            if (relX <= 2) return true
            if (relX >= 28) return true
        }
        return false
    }
}