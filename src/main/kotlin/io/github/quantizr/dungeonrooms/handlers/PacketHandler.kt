/*
 * Dungeon Rooms Mod - Secret Waypoints for Hypixel Skyblock Dungeons
 * Copyright 2021 Quantizr(_risk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.quantizr.dungeonrooms.handlers

import io.github.quantizr.dungeonrooms.events.PacketEvent.ReceiveEvent
import io.github.quantizr.dungeonrooms.events.PacketEvent.SendEvent
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge

class PacketHandler : ChannelDuplexHandler() {
    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is Packet<*>) MinecraftForge.EVENT_BUS.post(ReceiveEvent(msg))
        super.channelRead(ctx, msg)
    }

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is Packet<*>) MinecraftForge.EVENT_BUS.post(SendEvent(msg))
        super.write(ctx, msg, promise)
    }
}