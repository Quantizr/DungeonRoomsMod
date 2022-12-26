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

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.StringUtils
import kotlin.math.roundToInt

object TextRenderer : Gui() {
    @JvmStatic
    fun drawText(mc: Minecraft, text: String, x: Int, yy: Int, scale: Double, outline: Boolean) {
        var y = yy
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, scale)
        y -= mc.fontRendererObj.FONT_HEIGHT
        for (line in text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            y += (mc.fontRendererObj.FONT_HEIGHT * scale).toInt()
            if (outline) {
                val noColourLine = StringUtils.stripControlCodes(line)
                mc.fontRendererObj.drawString(
                    noColourLine, ((x / scale).roundToInt() - 1).toFloat(), (y / scale).roundToInt()
                        .toFloat(), 0x000000, false
                )
                mc.fontRendererObj.drawString(
                    noColourLine, ((x / scale).roundToInt() + 1).toFloat(), (y / scale).roundToInt()
                        .toFloat(), 0x000000, false
                )
                mc.fontRendererObj.drawString(
                    noColourLine,
                    (x / scale).roundToInt().toFloat(),
                    ((y / scale).roundToInt() - 1).toFloat(),
                    0x000000,
                    false
                )
                mc.fontRendererObj.drawString(
                    noColourLine,
                    (x / scale).roundToInt().toFloat(),
                    ((y / scale).roundToInt() + 1).toFloat(),
                    0x000000,
                    false
                )
                mc.fontRendererObj.drawString(
                    line, (x / scale).roundToInt().toFloat(), (y / scale).roundToInt()
                        .toFloat(), 0xFFFFFF, false
                )
            } else {
                mc.fontRendererObj.drawString(
                    line, (x / scale).roundToInt().toFloat(), (y / scale).roundToInt()
                        .toFloat(), 0xFFFFFF, true
                )
            }
        }
        GlStateManager.popMatrix()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
}