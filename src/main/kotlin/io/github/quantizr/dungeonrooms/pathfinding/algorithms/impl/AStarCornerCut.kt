package io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl

import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.AStarUtil.Node
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.IPathfinderAlgorithm
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*

class AStarCornerCut(private val room: BlockedChecker) : IPathfinderAlgorithm(room) {
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

    private var pfindIdx = 0


    private fun openNode(x: Int, y: Int, z: Int): Node {
        val cord = Vector3i(x, y, z)
        return nodeMap.getOrPut(cord) {
            Node(cord)
        }
    }

    private fun distSq(x: Float, y: Float, z: Float): Float {
        return MathHelper.sqrt_float(x * x + y * y + z * z)
    }


    override fun pathfind(from: Vector3i, to: Vector3d, timeout: Float): Boolean {
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

        pfindIdx++
        if (lastSx != (from.x * 2)
            || lastSy != (from.y * 2)
            || lastSz != (from.z * 2)
        ) {
            open.clear()
        }
        lastSx = (from.x * 2)
        lastSy = (from.y * 2)
        lastSz = (from.z * 2)

        val startNode = openNode(dx, dy, dz)
        val goalNode = openNode(lastSx, lastSy, lastSz)
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
            route.addLast(Vector3d(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z / 2.0))
            this.route = route
            return true
        }
        startNode.g = 0f
        startNode.f = 0f
        goalNode.g = Int.MAX_VALUE.toFloat()
        goalNode.f = Int.MAX_VALUE.toFloat()
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
            }
            if (n === goalNode) {
                // route = reconstructPath(startNode)
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
            for (z in -1..1) {
                for (y in -1..1) {
                    for (x in -1..1) {
                        if (x == 0 && y == 0 && z == 0) continue
                        val neighbor = openNode(
                            n?.coordinate!!.x + x, n.coordinate.y + y, n.coordinate.z + z
                        )

                        // check blocked.
                        if (!(destinationBB.minX <= neighbor.coordinate.x
                                    && neighbor.coordinate.x <= destinationBB.maxX
                                    && destinationBB.minY <= neighbor.coordinate.y
                                    && neighbor.coordinate.y <= destinationBB.maxY
                                    && destinationBB.minZ <= neighbor.coordinate.z
                                    && neighbor.coordinate.z <= destinationBB.maxZ // near destination
                                    ||
                                    !roomAccessor.isBlocked(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z)
                                )
                        ) { // not blocked
                            continue
                        }
                        if (neighbor.cacheMarker == pfindIdx) continue
                        val gScore =
                            n.g.plus(
                                MathHelper.sqrt_float((x * x + y * y + z * z).toFloat())
                            ) // altho it's sq, it should be fine
                        if (gScore < neighbor.g) {
                            neighbor.parent = n
                            neighbor.g = gScore
                            neighbor.f = gScore + distSq(
                                (goalNode.coordinate.x - neighbor.coordinate.x).toFloat(),
                                (goalNode.coordinate.y - neighbor.coordinate.y).toFloat(),
                                (goalNode.coordinate.z - neighbor.coordinate.z).toFloat()
                            )
                            open.add(neighbor)
                        } else if (neighbor.cacheMarker != pfindIdx) {
                            neighbor.f = gScore + distSq(
                                (goalNode.coordinate.x - neighbor.coordinate.x).toFloat(),
                                (goalNode.coordinate.y - neighbor.coordinate.y).toFloat(),
                                (goalNode.coordinate.z - neighbor.coordinate.z).toFloat()
                            )
                            open.add(neighbor)
                        }
                    }
                }
            }
        }
        return true
    }
}