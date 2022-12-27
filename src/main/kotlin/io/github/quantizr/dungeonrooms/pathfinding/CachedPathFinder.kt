package io.github.quantizr.dungeonrooms.pathfinding

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.quantizr.dungeonrooms.DungeonRooms
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AlgBuilder
import io.github.quantizr.dungeonrooms.test.PathfindTest
import io.github.quantizr.dungeonrooms.utils.BlockCache
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.joml.Vector3d
import org.joml.Vector3i
import java.time.Duration
import java.util.*
import java.util.concurrent.Future
import kotlin.math.floor


object CachedPathFinder {

    @Suppress("UnstableApiUsage")
    private val cache = Caffeine.newBuilder()
        .maximumSize(500)
        .build { job: PfJob ->
            val stat = AlgBuilder.buildPfStrategy(job.room)
            val now = System.nanoTime()
            stat.pathfind(job)
            if (DungeonRooms.debug) {
                PathfindTest.textToDisplay = listOf("Pathfinding took: ${(System.nanoTime() - now) / 1000000}ms")
            }
            return@build PfPath(stat.route)
        }

    private val defaultAccessor: BlockedChecker = object : BlockedChecker {
        val playerWidth = 0.3f
        val preBuilt = Blocks.stone.getStateFromMeta(2)

        private val cache = Caffeine.newBuilder()
            .maximumSize(500_000)
            .expireAfterWrite(Duration.ofSeconds(6))
            .build { key: Vector3i ->
                if(Minecraft.getMinecraft().theWorld == null) return@build null
                val wX = key.x / 2.0f
                val wY = key.y / 2.0f
                val wZ = key.z / 2.0f
                val bb = AxisAlignedBB.fromBounds(
                    (wX - playerWidth).toDouble(),
                    wY.toDouble(),
                    (wZ - playerWidth).toDouble(),
                    (wX + playerWidth).toDouble(),
                    (wY + 1.9f).toDouble(),
                    (wZ + playerWidth).toDouble()
                )

                val i = floor(bb.minX).toInt()
                val j = floor(bb.maxX + 1.0).toInt()
                val k = floor(bb.minY).toInt()
                val l = floor(bb.maxY + 1.0).toInt()
                val i1 = floor(bb.minZ).toInt()
                val j1 = floor(bb.maxZ + 1.0).toInt()
                val blockPos = BlockPos.MutableBlockPos()
                val list = ArrayList<AxisAlignedBB>()
                for (k1 in i until j) {
                    for (l1 in i1 until j1) {
                        for (i2 in k - 1 until l) {
                            blockPos[k1, i2] = l1
                            val blxState = BlockCache.getBlockState(blockPos)!!
                            if (blxState.block.material.blocksMovement()) {
                                if (!blxState.block.isFullCube || i2 != k - 1) {
                                    if (blxState != preBuilt) {
                                        if (blxState.block.isFullCube) {
                                            return@build true
                                        }
                                        try {
                                            blxState.block.addCollisionBoxesToList(
                                                Minecraft.getMinecraft().theWorld,
                                                blockPos,
                                                blxState,
                                                bb,
                                                list,
                                                null
                                            )
                                        } catch (e: Exception) {
                                            return@build true
                                        }
                                        if (list.isNotEmpty()) {
                                            return@build true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return@build false
            }

        override fun isBlocked(x: Int, y: Int, z: Int): Boolean {
            return cache[Vector3i(x, y, z)]!!
        }

    }


    fun CreatePath(
        entityIn: Entity,
        targetPos: Vector3i,
        room: BlockedChecker = defaultAccessor
    ): Future<List<Vector3d>> {
        return DungeonRooms.instance.ex.submit<List<Vector3d>> {
            return@submit cache[PfJob(
                Vector3i(entityIn.posX.toInt(), entityIn.posY.toInt(), entityIn.posZ.toInt()),
                Vector3d(targetPos).add(.5, .5, .5),
                room
            )]!!.path
        }
    }


}