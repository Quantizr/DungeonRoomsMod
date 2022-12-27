package io.github.quantizr.dungeonrooms.utils

import com.github.benmanes.caffeine.cache.Caffeine
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.joml.Vector3d
import org.joml.Vector3i
import java.time.Duration

object BlockCache {
    private val cache = Caffeine.newBuilder()
        .maximumSize(30_000)
        .expireAfterWrite(Duration.ofMinutes(1))
        .build { key: BlockPos? ->
            Minecraft.getMinecraft().theWorld.getBlockState(key)
        }

    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load?) {
        cache.invalidateAll()
    }

    fun getBlockState(pos: Vector3d): IBlockState? {
        return cache[BlockPos(pos.x, pos.y, pos.z)]
    }

    fun getBlockState(pos: BlockPos): IBlockState? {
        return cache[pos]
    }

    fun getBlockState(pos: Vector3i): IBlockState? {
        return cache[BlockPos(pos.x, pos.y, pos.z)]
    }

    fun getBlock(pos: Vector3i): Block {
        return cache[BlockPos(pos.x, pos.y, pos.z)]!!.block
    }

    fun getBlock(pos: BlockPos): Block {
        return cache[pos]!!.block
    }

}