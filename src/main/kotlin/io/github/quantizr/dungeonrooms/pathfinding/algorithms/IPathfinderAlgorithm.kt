package io.github.quantizr.dungeonrooms.pathfinding.algorithms

import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.PfJob
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*


abstract class IPathfinderAlgorithm(open val roomAccessor: BlockedChecker) {
    var route = LinkedList<Vector3d>()
        protected set

    abstract fun pathfind(from: Vector3i, to:Vector3d, timeout: Float = 2000f): Boolean
    fun pathfind(job: PfJob): Boolean {
        return pathfind(job.from, job.to)
    }
}