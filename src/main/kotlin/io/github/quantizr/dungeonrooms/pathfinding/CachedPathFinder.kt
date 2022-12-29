package io.github.quantizr.dungeonrooms.pathfinding

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.CachedAlgBuilder
import io.github.quantizr.dungeonrooms.test.PathfindTest
import io.github.quantizr.dungeonrooms.utils.BlockCache
import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor


class CachedPathFinder {

    private val algBuilder = CachedAlgBuilder(this)

    private val pathfindCache = Caffeine.newBuilder()
        .maximumSize(500)
        .build { job: PfJob ->
            val stat = algBuilder.buildPfAlg(job.to)
            val now = System.nanoTime()
            stat.pathfind(job)
            if (DRMConfig.debug) {
                PathfindTest.textToDisplay = listOf("Pathfinding took: ${(System.nanoTime() - now) / 1000000}ms")
            }
            return@build PfPath(job.id, stat.route)
        }

    val defaultAccessor: BlockedChecker = object : BlockedChecker {
        val playerWidth = 0.3f
        val preBuilt = Blocks.stone.getStateFromMeta(2)

        private val cache = Caffeine.newBuilder()
            .maximumSize(250_000)
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
                            val blockState = BlockCache.getBlockState(blockPos) ?: return@build true
                            if (blockState.block.material.blocksMovement()) {
                                if (!blockState.block.isFullCube || i2 != k - 1) {
                                    if (blockState != preBuilt) {
                                        if (blockState.block.isFullCube) {
                                            return@build true
                                        }
                                        try {
                                            blockState.block.addCollisionBoxesToList(
                                                Minecraft.getMinecraft().theWorld,
                                                blockPos,
                                                blockState,
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

    private val scope = CoroutineScope(Dispatchers.Default)

    fun destroy() {
        scope.cancel()
        pathfindCache.invalidateAll()
        algBuilder.destroy()
    }

    private val currProcesed: MutableMap<Vector3i, Boolean> = ConcurrentHashMap()

    /**
     * [lockProcessingThisTarget] if true prevents multiple pathfinds from being processed at the same time
     * to one destination
     */
    fun createPathAsync(
        start: Vector3i,
        target: Vector3i,
        room: BlockedChecker = defaultAccessor,
        id: String = UUID.randomUUID().toString(),
        lockProcessingThisTarget: Boolean = false,
        callback: (PfPath) -> Unit = {}
    ) {
        if(lockProcessingThisTarget) {
            if (currProcesed[target] == true) {
                return
            }
            currProcesed[target] = true
        }
        scope.async {
            val path = pathfindCache[PfJob(
                start,
                Vector3d(target).add(.5, .5, .5),
                room,
                id
            )]!!
            callback(path)
            if(lockProcessingThisTarget) currProcesed[target] = false
        }
    }


}