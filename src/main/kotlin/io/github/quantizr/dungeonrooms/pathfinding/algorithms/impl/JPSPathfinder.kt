package io.github.quantizr.dungeonrooms.pathfinding.algorithms.impl

import io.github.quantizr.dungeonrooms.pathfinding.BlockedChecker
import io.github.quantizr.dungeonrooms.pathfinding.algorithms.IPathfinderAlgorithm
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*
import kotlin.math.abs

class JPSPathfinder(room: BlockedChecker) : IPathfinderAlgorithm(room) {
    private val nodeMap: HashMap<Int, Node> = HashMap()


    private val within = 1.5f

    private val open = PriorityQueue(
        Comparator.comparing { a: Node? -> a?.f ?: Float.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.x ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.y ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.z ?: Int.MAX_VALUE }
    )


    private var destinationBB: AxisAlignedBB? = null
    private var tx = 0
    private var ty = 0
    private var tz = 0
    private fun openNode(x: Int, y: Int, z: Int): Node {
        val i = Node.makeHash(x, y, z)
        var node = nodeMap[i]
        if (node == null) {
            node = Node(x, y, z)
            nodeMap[i] = node
        }
        return node
    }

    private fun addNode(parent: Node, jumpPt: Node, addToOpen: Boolean): Node {
        val ng = parent.g + distSq(
            (jumpPt.x - parent.x).toFloat(),
            (jumpPt.y - parent.y).toFloat(),
            (jumpPt.z - parent.z).toFloat()
        )
        if (ng < jumpPt.g) {
            if (addToOpen) {
                open.remove(jumpPt)
            }
            jumpPt.g = ng
            jumpPt.h = if (jumpPt.h == -1f) distSq(
                (tx - jumpPt.x).toFloat(),
                (ty - jumpPt.y).toFloat(),
                (tz - jumpPt.z).toFloat()
            ) else jumpPt.h
            jumpPt.f = jumpPt.h + jumpPt.g
            jumpPt.parent = parent
            if (addToOpen) open.add(jumpPt)
        }
        return jumpPt
    }

    private fun distSq(x: Float, y: Float, z: Float): Float {
        return MathHelper.sqrt_float(x * x + y * y + z * z)
    }

    fun getNeighbors(prevN: Node, n: Node): Set<Node> {

        val d = Vector3i(
            MathHelper.clamp_int(n.x - prevN.x, -1, 1),
            MathHelper.clamp_int(n.y - prevN.y, -1, 1),
            MathHelper.clamp_int(n.z - prevN.z, -1, 1)
        )
        val c = Vector3i(n.x, n.y, n.z)
        val next = Vector3i(n.x + d.x, n.y + d.y, n.z + d.z)

        val nexts: MutableSet<Node> = HashSet()
        when (abs(d.x) + abs(d.y) + abs(d.z)) {
            0 -> {
                for (i in -1..1) for (j in -1..1) for (k in -1..1) {
                    if (i == 0 && j == 0 && k == 0) continue
                    nexts.add(openNode(c.x + i, c.y + j, c.z + k))
                }
            }

            1 -> {
                nexts.add(openNode(next.x, next.y, next.z))
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i == 0 && j == 0) continue
                        if (d.x != 0 && roomAccessor.isBlocked(c.x, c.y + i, c.z + j)) nexts.add(
                            openNode(
                                next.x,
                                c.y + i,
                                c.z + j
                            )
                        )
                        if (d.y != 0 && roomAccessor.isBlocked(c.x + i, c.y, c.z + j)) nexts.add(
                            openNode(
                                c.x + i,
                                next.y,
                                c.z + j
                            )
                        )
                        if (d.z != 0 && roomAccessor.isBlocked(c.x + i, c.y + j, c.z)) nexts.add(
                            openNode(
                                c.x + i,
                                c.y + j,
                                next.z
                            )
                        )
                    }
                }
            }

            2 -> {
                if (d.z != 0) nexts.add(openNode(c.x, c.y, next.z))
                if (d.y != 0) nexts.add(openNode(c.x, next.y, c.z))
                if (d.x != 0) nexts.add(openNode(next.x, c.y, c.z))
                nexts.add(openNode(next.x, next.y, next.z))
                if (d.x == 0) {
                    if (roomAccessor.isBlocked(c.x, c.y, c.z - d.z)) {
                        nexts.add(openNode(c.x, next.y, c.z - d.z))
                        if (roomAccessor.isBlocked(c.x + 1, c.y, c.z - d.z)) nexts.add(
                            openNode(
                                c.x + 1,
                                next.y,
                                c.z - d.z
                            )
                        )
                        if (roomAccessor.isBlocked(c.x - 1, c.y, c.z - d.z)) nexts.add(
                            openNode(
                                c.x - 1,
                                next.y,
                                c.z - d.z
                            )
                        )
                    }
                    if (roomAccessor.isBlocked(c.x, c.y - d.y, c.z)) {
                        nexts.add(openNode(c.x, c.y - d.y, next.z))
                        if (roomAccessor.isBlocked(c.x + 1, c.y - d.y, c.z)) nexts.add(
                            openNode(
                                c.x + 1,
                                c.y - d.y,
                                next.z
                            )
                        )
                        if (roomAccessor.isBlocked(c.x - 1, c.y - d.y, c.z)) nexts.add(
                            openNode(
                                c.x + 1,
                                c.y - d.y,
                                next.z
                            )
                        )
                    }
                } else if (d.y == 0) {
                    if (roomAccessor.isBlocked(c.x, c.y, c.z - d.z)) {
                        nexts.add(openNode(next.x, c.y, c.z - d.z))
                        if (roomAccessor.isBlocked(c.x, c.y + 1, c.z - d.z)) nexts.add(
                            openNode(
                                next.x,
                                c.y + 1,
                                c.z - d.z
                            )
                        )
                        if (roomAccessor.isBlocked(c.x, c.y - 1, c.z - d.z)) nexts.add(
                            openNode(
                                next.x,
                                c.y - 1,
                                c.z - d.z
                            )
                        )
                    }
                    if (roomAccessor.isBlocked(c.x - d.x, c.y, c.z)) {
                        nexts.add(openNode(c.x - d.x, c.y, next.z))
                        if (roomAccessor.isBlocked(c.x - d.x, c.y + 1, c.z)) nexts.add(
                            openNode(
                                c.x - d.x,
                                c.y + 1,
                                next.z
                            )
                        )
                        if (roomAccessor.isBlocked(c.x - d.x, c.y - 1, c.z)) nexts.add(
                            openNode(
                                c.x - d.x,
                                c.y - 1,
                                next.z
                            )
                        )
                    }
                } else if (d.z == 0) {
                    if (roomAccessor.isBlocked(c.x, c.y - d.y, c.z)) {
                        nexts.add(openNode(next.x, c.y - d.y, c.z))
                        if (roomAccessor.isBlocked(c.x, c.y - d.y, c.z + 1)) nexts.add(
                            openNode(
                                next.x,
                                c.y - d.y,
                                c.z + 1
                            )
                        )
                        if (roomAccessor.isBlocked(c.x, c.y - d.y, c.z - 1)) nexts.add(
                            openNode(
                                next.x,
                                c.y - d.y,
                                c.z - 1
                            )
                        )
                    }
                    if (roomAccessor.isBlocked(c.x - d.x, c.y, c.z)) {
                        nexts.add(openNode(c.x - d.x, next.y, c.z))
                        if (roomAccessor.isBlocked(c.x - d.x, c.y, c.z + 1)) nexts.add(
                            openNode(
                                c.x - d.x,
                                next.y,
                                c.z + 1
                            )
                        )
                        if (roomAccessor.isBlocked(c.x - d.x, c.y, c.z - 1)) nexts.add(
                            openNode(
                                c.x - d.x,
                                next.y,
                                c.z - 1
                            )
                        )
                    }
                }
            }

            3 -> {
                nexts.add(openNode(c.x, c.y, next.z))
                nexts.add(openNode(c.x, next.y, c.z))
                nexts.add(openNode(next.x, c.y, c.z))
                nexts.add(openNode(next.x, c.y, next.z))
                nexts.add(openNode(c.x, next.y, next.z))
                nexts.add(openNode(next.x, next.y, c.z))
                nexts.add(openNode(next.x, next.y, next.z))
                if (roomAccessor.isBlocked(c.x, c.y, c.z - d.z)) {
                    nexts.add(openNode(c.x, next.y, c.z - d.z))
                    nexts.add(openNode(next.x, next.y, c.z - d.z))
                    nexts.add(openNode(next.x, c.y, c.z - d.z))
                }
                if (roomAccessor.isBlocked(c.x - d.x, c.y, c.z)) {
                    nexts.add(openNode(c.x - d.x, next.y, next.z))
                    nexts.add(openNode(c.x - d.x, next.y, c.z))
                    nexts.add(openNode(c.x - d.x, c.y, next.z))
                }
                if (roomAccessor.isBlocked(c.x, c.y - d.y, c.z)) {
                    nexts.add(openNode(c.x, c.y - d.y, next.z))
                    nexts.add(openNode(next.x, c.y - d.y, c.z))
                    nexts.add(openNode(next.x, c.y - d.y, next.z))
                }
            }
        }
        return nexts
    }

    fun expand(x: Int, y: Int, z: Int, dx: Int, dy: Int, dz: Int): Node? {
        var x = x
        var y = y
        var z = z
        while (true) {
            val nx = x + dx
            val ny = y + dy
            val nz = z + dz
            if (roomAccessor.isBlocked(nx, ny, nz)) return null
            if (nx > destinationBB!!.minX && nx < destinationBB!!.maxX && ny > destinationBB!!.minY && ny < destinationBB!!.maxY && nz > destinationBB!!.minZ && nz < destinationBB!!.maxZ) return openNode(
                nx,
                ny,
                nz
            )
            val determinant = abs(dx) + abs(dy) + abs(dz)
            if (determinant == 1) {
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i == 0 && j == 0) continue
                        if (dx != 0 && roomAccessor.isBlocked(nx, ny + i, nz + j) && !roomAccessor.isBlocked(
                                nx + dx,
                                ny + i,
                                nz + j
                            )
                        ) return openNode(nx, ny, nz)
                        if (dy != 0 && roomAccessor.isBlocked(nx + i, ny, nz + j) && !roomAccessor.isBlocked(
                                nx + i,
                                ny + dy,
                                nz + j
                            )
                        ) return openNode(nx, ny, nz)
                        if (dz != 0 && roomAccessor.isBlocked(nx + i, ny + j, nz) && !roomAccessor.isBlocked(
                                nx + i,
                                ny + j,
                                nz + dz
                            )
                        ) return openNode(nx, ny, nz)
                    }
                }
            } else if (determinant == 2) {
                for (value in EnumFacing.VALUES) {
                    if (value.frontOffsetX == dx || value.frontOffsetY == dy || value.frontOffsetZ == dz) continue
                    val tx = nx + value.frontOffsetX
                    val ty = ny + value.frontOffsetY
                    val tz = nz + value.frontOffsetZ
                    if (roomAccessor.isBlocked(tx, ty, tz)) return openNode(nx, ny, nz)
                }
                if (dx != 0 && expand(nx, ny, nz, dx, 0, 0) != null) return openNode(nx, ny, nz)
                if (dy != 0 && expand(nx, ny, nz, 0, dy, 0) != null) return openNode(nx, ny, nz)
                if (dz != 0 && expand(nx, ny, nz, 0, 0, dz) != null) return openNode(nx, ny, nz)
            } else if (determinant == 3) {
                if (roomAccessor.isBlocked(x, ny, nz) || roomAccessor.isBlocked(nx, y, nz) || roomAccessor.isBlocked(
                        nx,
                        ny,
                        z
                    )
                ) return openNode(nx, ny, nz)
                if (expand(nx, ny, nz, dx, 0, 0) != null || expand(nx, ny, nz, dx, dy, 0) != null || expand(
                        nx,
                        ny,
                        nz,
                        dx,
                        0,
                        dz
                    ) != null || expand(nx, ny, nz, 0, dy, 0) != null || expand(
                        nx,
                        ny,
                        nz,
                        0,
                        dy,
                        dz
                    ) != null || expand(nx, ny, nz, 0, 0, dz) != null
                ) return openNode(nx, ny, nz)
            }
            x = nx
            y = ny
            z = nz
        }
    }

    class Node(val x: Int, val y: Int, val z: Int) {
        var f = 0f
        var g = Float.MAX_VALUE
        var h = -1f
        var closed = false

        var parent: Node? = null
        fun close(): Node {
            closed = true
            return this
        }

        companion object {
            fun makeHash(x: Int, y: Int, z: Int): Int {
                return y and 255 or (x and 32767 shl 8) or (z and 32767 shl 24) or (if (x < 0) Int.MIN_VALUE else 0) or if (z < 0) 32768 else 0
            }
        }
    }

    override fun pathfind(from: Vector3i, to: Vector3d, timeout: Float): Boolean {
        route.clear()
        nodeMap.clear()
        var from = Vector3d((from.x * 2) / 2.0, (from.y * 2) / 2.0, (from.z * 2) / 2.0)
        var to = Vector3d((to.x * 2).toInt() / 2.0, (to.y * 2).toInt() / 2.0, (to.z * 2).toInt() / 2.0)

        tx = (to.x * 2).toInt()
        ty = (to.y * 2).toInt()
        tz = (to.z * 2).toInt()
        destinationBB = AxisAlignedBB.fromBounds(
            (to.x - within) * 2,
            (to.y - within) * 2,
            (to.z - within) * 2,
            (to.x + within) * 2,
            (to.y + within) * 2,
            (to.z + within) * 2
        )
        open.clear()
        var start: Node
        open.add(
            openNode(
                from.x.toInt() * 2 + 1,
                from.y.toInt() * 2,
                from.z.toInt() * 2 + 1
            ).also { start = it })
        start.g = 0f
        start.f = 0f
        start.h = from.distanceSquared(to).toFloat()
        var end: Node? = null
        var minDist = Float.MAX_VALUE
        val forceEnd = System.currentTimeMillis() + timeout + 999999999L
        while (!open.isEmpty()) {
            if (forceEnd < System.currentTimeMillis() && timeout != -1F) break
            val n = open.poll()
            if (n != null) {
                n.closed = true
                if (minDist > n.h) {
                    minDist = n.h
                    end = n
                }
                if (n.x > destinationBB!!.minX && n.x < destinationBB!!.maxX && n.y > destinationBB!!.minY && n.y < destinationBB!!.maxY && n.z > destinationBB!!.minZ && n.z < destinationBB!!.maxZ) {
                    break
                }
            }
            for (neighbor in getNeighbors(n!!.parent ?: n, n)) {
                val jumpPT = expand(n.x, n.y, n.z, neighbor.x - n.x, neighbor.y - n.y, neighbor.z - n.z)
                if (jumpPT == null || jumpPT.closed) continue
                addNode(n, jumpPT, true)
            }
        }
        if (end == null) {
            return false
        }
        var p = end
        while (p != null) {
            route.addLast(Vector3d((p.x / 2.0f).toDouble(), p.y / 2.0f + 0.1, (p.z / 2.0f).toDouble()))
            p = p.parent
        }
        return true
    }
}