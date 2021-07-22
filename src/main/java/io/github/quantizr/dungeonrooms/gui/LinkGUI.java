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

package io.github.quantizr.dungeonrooms.gui;

import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.RoomDetection;
import io.github.quantizr.dungeonrooms.handlers.OpenLink;
import io.github.quantizr.dungeonrooms.handlers.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;


public class LinkGUI extends GuiScreen {

    private GuiButton discordClient;
    private GuiButton discordBrowser;
    private GuiButton SBPSecrets;
    private GuiButton close;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int height = sr.getScaledHeight();
        int width = sr.getScaledWidth();

        discordClient = new GuiButton(0, width / 2 - 185, height / 6 + 96, 120, 20, "DSG Discord Client");
        discordBrowser = new GuiButton(1, width / 2 - 60, height / 6 + 96, 120, 20, "DSG Discord Browser");
        SBPSecrets = new GuiButton(2, width / 2 + 65, height / 6 + 96, 120, 20, "SBP Secrets Mod");
        close = new GuiButton(3, width / 2 - 60, height / 6 + 136, 120, 20, "Close");

        this.buttonList.add(discordClient);
        this.buttonList.add(discordBrowser);
        this.buttonList.add(SBPSecrets);
        this.buttonList.add(close);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        Minecraft mc = Minecraft.getMinecraft();

        String displayText;
        if (RoomDetection.roomName.equals("undefined")) {
            displayText = "Where would you like to view secrets for: " + EnumChatFormatting.RED + "undefined";
        } else {
            displayText = "Where would you like to view secrets for: " + EnumChatFormatting.GREEN + RoomDetection.roomName;
        }
        int displayWidth = mc.fontRendererObj.getStringWidth(displayText);
        TextRenderer.drawText(mc, displayText, width / 2 - displayWidth / 2, height / 6 + 56, 1D, false);

        String noteText = EnumChatFormatting.GRAY + "If you wish to have the hotkey go directly to DSG or SBP instead of this GUI run "
                + EnumChatFormatting.WHITE + "/room set <gui | dsg | sbp>";
        int noteWidth = mc.fontRendererObj.getStringWidth(noteText);
        TextRenderer.drawText(mc, noteText, width / 2 - noteWidth / 2, (int) (height*0.9), 1D, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (button == discordClient) {
            OpenLink.openDiscord("client");
            player.closeScreen();
        } else if (button == discordBrowser) {
            OpenLink.openDiscord("browser");
            player.closeScreen();
        } else if (button == SBPSecrets) {
            if (DungeonRooms.usingSBPSecrets) {
                OpenLink.openSBPSecrets();
            } else {
                String sbpURL = "https://discord.gg/2UjaFqfPwJ";
                ChatComponentText sbp = new ChatComponentText(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.UNDERLINE + sbpURL);
                sbp.setChatStyle(sbp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sbpURL)));
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: You need theSkyblock Personalized (SBP) Mod for this feature, get it from ").appendSibling(sbp));
            }
           player.closeScreen();
        } else if (button == close) {
           player.closeScreen();
        }
    }
}
