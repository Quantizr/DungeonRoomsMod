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

package io.github.quantizr.dungeonrooms.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;

public class TextRenderer extends Gui {
    public static void drawText(Minecraft mc, String text, int x, int y, double scale, boolean outline) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        y -= mc.fontRendererObj.FONT_HEIGHT;
        for (String line : text.split("\n")) {
            y += mc.fontRendererObj.FONT_HEIGHT * scale;
            if (outline) {
                String noColourLine = StringUtils.stripControlCodes(line);
                mc.fontRendererObj.drawString(noColourLine, (int) Math.round(x / scale) - 1, (int) Math.round(y / scale), 0x000000, false);
                mc.fontRendererObj.drawString(noColourLine, (int) Math.round(x / scale) + 1, (int) Math.round(y / scale), 0x000000, false);
                mc.fontRendererObj.drawString(noColourLine, (int) Math.round(x / scale), (int) Math.round(y / scale) - 1, 0x000000, false);
                mc.fontRendererObj.drawString(noColourLine, (int) Math.round(x / scale), (int) Math.round(y / scale) + 1, 0x000000, false);
                mc.fontRendererObj.drawString(line, (int) Math.round(x / scale), (int) Math.round(y / scale), 0xFFFFFF, false);
            } else {
                mc.fontRendererObj.drawString(line, (int) Math.round(x / scale), (int) Math.round(y / scale), 0xFFFFFF, true);
            }
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}

