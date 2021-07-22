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

package io.github.quantizr.dungeonrooms.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Taken from Skytils under the GNU Affero General Public License v3.0
 * https://github.com/Skytils/SkytilsMod/blob/0.x/LICENSE
 * @author My-Name-Is-Jeff (lily)
 */
@Cancelable
public class PacketEvent extends Event {

    public Direction direction;
    public Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class ReceiveEvent extends PacketEvent {
        public ReceiveEvent(Packet<?> packet) {
            super(packet);
            this.direction = Direction.INBOUND;
        }
    }

    public static class SendEvent extends PacketEvent {
        public SendEvent(Packet<?> packet) {
            super(packet);
            this.direction = Direction.OUTBOUND;
        }
    }

    enum Direction {
        INBOUND,
        OUTBOUND
    }

}