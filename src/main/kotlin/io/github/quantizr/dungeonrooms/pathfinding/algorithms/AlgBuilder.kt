package io.github.quantizr.dungeonrooms.pathfinding.algorithms

import io.github.quantizr.dungeonrooms.DRMConfig
import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.AStarCornerCut
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.AStarFineGrid
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.JPSPathfinder
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl.ThetaStar

object AlgBuilder {
    fun buildPfStrategy(roomAcessor: BlockedChecker, mode: Int): IPathfinderAlgorithm {
        return when (mode) {
            0 -> ThetaStar(roomAcessor)
            1 -> AStarCornerCut(roomAcessor)
            2 -> AStarFineGrid(roomAcessor)
            3 -> JPSPathfinder(roomAcessor)
            else -> ThetaStar(roomAcessor)
        }
    }

    fun buildPfStrategy(roomAcessor: BlockedChecker): IPathfinderAlgorithm {
        return buildPfStrategy(roomAcessor, DRMConfig.secretPathfindStrategy)
    }
}