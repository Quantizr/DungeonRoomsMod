package io.github.quantizr.dungeonrooms.pathfinding

import org.joml.Vector3d
import org.joml.Vector3i

data class PfJob(val from: Vector3i, val to: Vector3d, val room: BlockedChecker)