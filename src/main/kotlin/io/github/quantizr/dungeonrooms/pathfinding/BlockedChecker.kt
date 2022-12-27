package io.github.quantizr.dungeonrooms.pathfinding

interface BlockedChecker {
    fun isBlocked(x: Int, y: Int, z: Int): Boolean
}