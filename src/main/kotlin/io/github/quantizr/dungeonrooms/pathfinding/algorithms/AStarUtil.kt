package io.github.quantizr.dungeonrooms.pathfinding.algorithms

class AStarUtil {
    class Node(val coordinate: Coordinate) {
        var f = Float.MAX_VALUE
        var g = Float.MAX_VALUE
        var lastVisited = 0

        var parent: Node? = null
    }

    data class Coordinate(val x: Int = 0, val y: Int = 0, val z: Int = 0)
}