package io.github.quantizr.dungeonrooms.pathfinding.algorithms

import org.joml.Vector3i

class AStarUtil {
    class Node(val coordinate: Vector3i) {
        var f = Float.MAX_VALUE
        var g = Float.MAX_VALUE
        var lastVisited = 0

        var parent: Node? = null
    }
}