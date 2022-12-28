package io.github.quantizr.dungeonrooms.pathfinding.algorithms

import net.minecraft.util.MathHelper
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*
import kotlin.math.sqrt

object AStarUtil {
    class Node(val coordinate: Vector3i) {
        var f = Float.MAX_VALUE
        var g = Float.MAX_VALUE
        var cacheMarker = 0

        var parent: Node? = null
    }

    fun reconstructPath(current: Node): LinkedList<Vector3d> {
        val route = LinkedList<Vector3d>()
        var curr: Node? = current
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
        return route
    }

    fun distSq(x: Float, y: Float, z: Float): Float {
        return MathHelper.sqrt_float(x * x + y * y + z * z)
    }

    fun distSq(x: Int, y: Int, z: Int): Float {
        return sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }
}