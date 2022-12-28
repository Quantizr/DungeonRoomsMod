package io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl

import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil.Node
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil.reconstructPath
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.IPathfinderAlgorithm
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i
import java.util.*

class ThetaStar(room: BlockedChecker) : IPathfinderAlgorithm(room) {
    private var dx: Int = 0
    private var dy: Int = 0
    private var dz: Int = 0


    private lateinit var destinationBB: AxisAlignedBB
    private val nodeMap: MutableMap<Vector3i, Node> = HashMap()

    private val open = PriorityQueue(
        Comparator.comparing { a: Node? -> a?.f ?: Float.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.x ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.y ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.z ?: Int.MAX_VALUE }
    )

    private var lastSx = 0
    private var lastSy = 0
    private var lastSz = 0

    private var cacheMarker = 0

    private fun openNode(x: Int, y: Int, z: Int): Node {
        val coordinate = Vector3i(x, y, z)
        return nodeMap.computeIfAbsent(coordinate) { Node(coordinate) }
    }

    /**
     * raytrace and check if the path is blocked along the way
     */
    private fun lineofsight(a: Node?, b: Node?): Boolean {
        if (a == null || b == null) return false
        val s = Vector3f(a.coordinate)
        val e = Vector3f(b.coordinate)
        val dd = Vector3f()
        e.sub(s, dd)
        val len = dd.length()
        dd.normalize()

        var d = 0
        while (d <= len) {
            val x = s.x().toInt()
            val y = s.y().toInt()
            val z = s.z().toInt()
            if (roomAccessor.isBlocked(x, y, z)) return false
            if (roomAccessor.isBlocked(x + 1, y, z + 1)) return false
            if (roomAccessor.isBlocked(x - 1, y, z - 1)) return false
            if (roomAccessor.isBlocked(x + 1, y, z - 1)) return false
            if (roomAccessor.isBlocked(x - 1, y, z + 1)) return false
            s.add(dd)
            d += 1
        }
        return true

    }

    override fun pathfind(from: Vector3i, to: Vector3d, timeout: Float): Boolean {
        cacheMarker++

        dx = (to.x * 2).toInt()
        dy = (to.y * 2).toInt()
        dz = (to.z * 2).toInt()
        destinationBB = AxisAlignedBB.fromBounds(
            (dx - 2).toDouble(),
            (dy - 2).toDouble(),
            (dz - 2).toDouble(),
            (dx + 2).toDouble(),
            (dy + 2).toDouble(),
            (dz + 2).toDouble()
        )

        val i = from.x * 2
        val i1 = from.y * 2
        val i2 = from.z * 2
        if (lastSx != i || lastSy != i1 || lastSz != i2) {
            open.clear()
        }
        lastSx = i
        lastSy = i1
        lastSz = i2
        if (roomAccessor.isBlocked(lastSx, lastSy, lastSz)) return false
        val startNode = openNode(dx, dy, dz)
        val goalNode = openNode(lastSx, lastSy, lastSz)
        startNode.g = 0f
        startNode.f = 0f
        goalNode.g = Float.MAX_VALUE
        goalNode.f = Float.MAX_VALUE
        if (goalNode.parent != null) {
            this.route = reconstructPath(goalNode)
            return true
        }
        open.add(startNode)
        val end = System.currentTimeMillis() + timeout
        while (!open.isEmpty()) {
            if (System.currentTimeMillis() > end) {
                return false
            }
            val n = open.poll()


            if (n != null) {
                if (n.cacheMarker == cacheMarker) {
                    continue
                }
                n.cacheMarker = cacheMarker
            }

            if (n === goalNode) {
                this.route = reconstructPath(goalNode)
                return true
            }
            for (value in EnumFacing.VALUES) {
                n!!
                val neighbor = openNode(
                    n.coordinate.x + value.frontOffsetX,
                    n.coordinate.y + value.frontOffsetY,
                    n.coordinate.z + value.frontOffsetZ
                )

                if (destinationBB.minX <= neighbor.coordinate.x
                    && neighbor.coordinate.x <= destinationBB.maxX
                    && destinationBB.minY <= neighbor.coordinate.y
                    && neighbor.coordinate.y <= destinationBB.maxY
                    && destinationBB.minZ <= neighbor.coordinate.z
                    && neighbor.coordinate.z <= destinationBB.maxZ
                    || !roomAccessor.isBlocked(
                        neighbor.coordinate.x,
                        neighbor.coordinate.y,
                        neighbor.coordinate.z
                    )
                ) {
                    if (neighbor.cacheMarker != cacheMarker) {
                        var flag = false
                        val distSq = AStarUtil.distSq(
                            goalNode.coordinate.x - neighbor.coordinate.x,
                            goalNode.coordinate.y - neighbor.coordinate.y,
                            goalNode.coordinate.z - neighbor.coordinate.z
                        )
                        val parent = n.parent
                        if (parent != null) {
                            val tempGScore = parent.g + AStarUtil.distSq(
                                parent.coordinate.x - neighbor.coordinate.x,
                                parent.coordinate.y - neighbor.coordinate.y,
                                parent.coordinate.z - neighbor.coordinate.z
                            )
                            if (tempGScore < neighbor.g && lineofsight(parent, neighbor)) {
                                neighbor.parent = parent
                                neighbor.g = tempGScore
                                neighbor.f = tempGScore + distSq
                                open.add(neighbor)
                                flag = true
                            }
                        }
                        if (!flag) {
                            val gScore = n.g + 1
                            if (gScore < neighbor.g) {
                                neighbor.parent = n
                                neighbor.g = gScore
                                neighbor.f = gScore + distSq
                                open.add(neighbor)
                            } else if (neighbor.cacheMarker != cacheMarker) {
                                neighbor.f = neighbor.g + distSq
                                open.add(neighbor)
                            }
                        }
                    }
                }
            }
        }
        return true
    }

}