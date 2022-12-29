package io.github.quantizr.dungeonrooms.pathfinding.algorithms

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.pathfinding.CachedPathFinder
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.AStarCornerCut
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.AStarFineGrid
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.ThetaStar
import org.joml.Vector3d
import java.time.Duration

class CachedAlgBuilder(val parent: CachedPathFinder) {
    // since we pathfind to the same destination and the world does not change,
    // we can cache open nodes in the A* family of algorithms
    private val cache = Caffeine.newBuilder()
        .maximumSize(11)
        .expireAfterWrite(Duration.ofMinutes(2))
        .build { _: Vector3d ->
            return@build when (DRMConfig.secretPathfindStrategy) {
                0 -> ThetaStar(parent.defaultAccessor)
                1 -> AStarCornerCut(parent.defaultAccessor)
                2 -> AStarFineGrid(parent.defaultAccessor)
                else -> ThetaStar(parent.defaultAccessor)
            }
        }

    fun buildPfAlg(destination: Vector3d): IPathfinderAlgorithm {
        return cache.get(destination)!!
    }

    fun destroy() {
        cache.invalidateAll()
    }
}