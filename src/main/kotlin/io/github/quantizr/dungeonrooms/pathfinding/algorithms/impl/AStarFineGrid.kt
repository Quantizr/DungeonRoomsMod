package io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl

import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil.Node
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.IPathfinderAlgorithm
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*

class AStarFineGrid(room: BlockedChecker) : IPathfinderAlgorithm(room) {
    var dx: Int = 0
    var dy: Int = 0
    var dz: Int = 0


    private lateinit var destinationBB: AxisAlignedBB
    private val nodeMap: MutableMap<Vector3i, Node> = HashMap()

    private val open = PriorityQueue(
        Comparator.comparing { a: Node? -> a?.f ?: Float.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.x ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.y ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.coordinate?.z ?: Int.MAX_VALUE }
    )

    var lastSx = 0
    var lastSy = 0
    var lastSz = 0


    override fun pathfind(from: Vector3i, to: Vector3d, timeout: Float): Boolean {
        pfindIdx++

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

        if (lastSx != from.x * 2 || lastSy != from.y * 2 || lastSz != from.z * 2
        ) open.clear()
        lastSx = from.x * 2
        lastSy = from.y * 2
        lastSz = from.z * 2
        val startNode = openNode(dx, dy, dz)
        val goalNode = openNode(lastSx, lastSy, lastSz)
        startNode.g = 0f
        startNode.f = 0f
        goalNode.g = Int.MAX_VALUE.toFloat()
        goalNode.f = Int.MAX_VALUE.toFloat()
        if (goalNode.parent != null) {
            val route = LinkedList<Vector3d>()
            var curr: Node? = goalNode
            while (curr!!.parent != null) {
                route.addLast(
                    Vector3d(
                        curr.coordinate.x / 2.0,
                        curr.coordinate.y / 2.0 + 0.1,
                        curr.coordinate.z / 2.0
                    )
                )
                curr = curr.parent
            }
            route.addLast(
                Vector3d(
                    curr.coordinate.x / 2.0,
                    curr.coordinate.y / 2.0 + 0.1,
                    curr.coordinate.z / 2.0
                )
            )
            this.route = route
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
                if (n.cacheMarker == pfindIdx) continue
                n.cacheMarker = pfindIdx
                if (n === goalNode) {
                    val route = LinkedList<Vector3d>()
                    var curr: Node? = goalNode
                    while (curr!!.parent != null) {
                        route.addLast(
                            Vector3d(
                                curr.coordinate.x / 2.0,
                                curr.coordinate.y / 2.0 + 0.1,
                                curr.coordinate.z / 2.0
                            )
                        )
                        curr = curr.parent
                    }
                    route.addLast(
                        Vector3d(
                            curr.coordinate.x / 2.0,
                            curr.coordinate.y / 2.0 + 0.1,
                            curr.coordinate.z / 2.0
                        )
                    )
                    this.route = route
                    return true
                }
            }
            for (value in EnumFacing.VALUES) {
                val neighbor = openNode(
                    n!!.coordinate.x + value.frontOffsetX,
                    n.coordinate.y + value.frontOffsetY,
                    n.coordinate.z + value.frontOffsetZ
                )

                // check blocked.
                if (destinationBB.minX <= neighbor.coordinate.x
                    && neighbor.coordinate.x <= destinationBB.maxX
                    && destinationBB.minY <= neighbor.coordinate.y
                    && neighbor.coordinate.y <= destinationBB.maxY
                    && destinationBB.minZ <= neighbor.coordinate.z
                    && neighbor.coordinate.z <= destinationBB.maxZ // near destination
                    || !roomAccessor.isBlocked(
                        neighbor.coordinate.x,
                        neighbor.coordinate.y,
                        neighbor.coordinate.z
                    )
                ) {
                    val gScore = n.g + 1
                    val distSq = AStarUtil.distSq(
                        (goalNode.coordinate.x - neighbor.coordinate.x).toFloat(),
                        (goalNode.coordinate.y - neighbor.coordinate.y).toFloat(),
                        (goalNode.coordinate.z - neighbor.coordinate.z).toFloat()
                    )
                    if (gScore < neighbor.g) {
                        neighbor.parent = n
                        neighbor.g = gScore
                        neighbor.f = gScore + distSq
                        open.add(neighbor)
                    } else if (neighbor.cacheMarker != pfindIdx) {
                        neighbor.f = gScore + distSq
                        open.add(neighbor)
                    }
                }
                // not blocked
            }
        }
        return true
    }

    private var pfindIdx = 0


    private fun openNode(x: Int, y: Int, z: Int): Node {
        val coordinate = Vector3i(x, y, z)
        var node = nodeMap[coordinate]
        if (node == null) {
            node = Node(coordinate)
            nodeMap[coordinate] = node
        }
        return node
    }

}