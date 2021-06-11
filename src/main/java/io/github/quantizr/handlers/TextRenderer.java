/*
Taken from Danker's Skyblock Mod (https://github.com/bowser0000/SkyblockMod/).
This file was released under GNU General Public License v3.0 and remains under said license.
Modified by Quantizr (_risk) in Feb. 2021.
*/

package io.github.quantizr.handlers;

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

