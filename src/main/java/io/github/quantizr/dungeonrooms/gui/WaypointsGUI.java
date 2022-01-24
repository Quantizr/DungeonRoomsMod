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
import io.github.quantizr.dungeonrooms.dungeons.catacombs.Waypoints;
import io.github.quantizr.dungeonrooms.handlers.ConfigHandler;
import io.github.quantizr.dungeonrooms.handlers.TextRenderer;
import io.github.quantizr.dungeonrooms.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointsGUI extends GuiScreen {

    private GuiButton waypointsEnabled;
    private GuiButton practiceModeEnabled;
    private GuiButton showEntrance;
    private GuiButton showSuperboom;
    private GuiButton showSecrets;
    private GuiButton showFairySouls;
    private GuiButton showStonk;
    private GuiButton disableWhenAllFound;
    private GuiButton sneakToDisable;
    private GuiButton close;

    public static List<GuiButton> secretButtonList = new ArrayList<>(Arrays.asList(new GuiButton[10]));

    private static boolean waypointGuiOpened = false;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        waypointGuiOpened = true;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int height = sr.getScaledHeight();
        int width = sr.getScaledWidth();

        waypointsEnabled = new GuiButton(0, (width / 2 - 100) + 0, height / 6 - 5, 200, 20, waypointBtnText());
        practiceModeEnabled = new GuiButton(1, (width / 2 - 100) - 110, height / 6 + 25, 200, 20, "Practice Mode: " + getOnOff(Waypoints.practiceModeOn));
        showEntrance = new GuiButton(2, (width / 2 - 100) + 110, height / 6 + 25, 200, 20, "Show Entrance Waypoints: " + getOnOff(Waypoints.showEntrance));
        showSuperboom = new GuiButton(3, (width / 2 - 100) - 110, height / 6 + 55, 200, 20, "Show Superboom Waypoints: " + getOnOff(Waypoints.showSuperboom));
        showSecrets = new GuiButton(4, (width / 2 - 100) + 110, height / 6 + 55, 200, 20, "Show Secret Waypoints: " + getOnOff(Waypoints.showSecrets));
        showFairySouls = new GuiButton(5, (width / 2 - 100) - 110, height / 6 + 85, 200, 20, "Show Fairy Soul Waypoints: " + getOnOff(Waypoints.showFairySouls));
        showStonk = new GuiButton(6, (width / 2 - 100) + 110, height / 6 + 85, 200, 20, "Show Stonk Waypoints: " + getOnOff(Waypoints.showStonk));
        sneakToDisable = new GuiButton(7, (width / 2 - 100) - 110, height / 6 + 115, 200, 20, "Double-Tap Sneak to Hide Nearby: " + getOnOff(Waypoints.sneakToDisable));
        disableWhenAllFound = new GuiButton(8, (width / 2 - 100) + 110, height / 6 + 115, 200, 20, "Disable when all secrets found: " + getOnOff(Waypoints.disableWhenAllFound));
        close = new GuiButton(9, width / 2 - 100, (height / 6) * 5, 200, 20, "Close");

        this.buttonList.add(waypointsEnabled);
        this.buttonList.add(practiceModeEnabled);
        this.buttonList.add(showEntrance);
        this.buttonList.add(showSuperboom);
        this.buttonList.add(showSecrets);
        this.buttonList.add(showFairySouls);
        this.buttonList.add(showStonk);
        this.buttonList.add(sneakToDisable);
        this.buttonList.add(disableWhenAllFound);
        this.buttonList.add(close);

        if (Utils.inCatacombs) {
            if (Waypoints.secretNum > 0) {
                if (Waypoints.secretNum <= 5) {
                    for (int i = 1; i <= Waypoints.secretNum; i++) {
                        int adjustPos = (-40 * (Waypoints.secretNum)) - 70 + (80 * i);
                        secretButtonList.set(i - 1, new GuiButton(10 + i, (width / 2) + adjustPos, height / 6 + 170, 60, 20, i + ": " + getOnOff(Waypoints.secretsList.get(i - 1))));
                        this.buttonList.add(secretButtonList.get(i - 1));
                    }
                } else {
                    for (int i = 1; i <= (int) Math.ceil((double) Waypoints.secretNum / 2); i++) {
                        int adjustPos = (-40 * ((int) Math.ceil((double) Waypoints.secretNum / 2))) - 70 + (80 * i);
                        secretButtonList.set(i - 1, new GuiButton(10 + i, (width / 2) + adjustPos, height / 6 + 170, 60, 20, i + ": " + getOnOff(Waypoints.secretsList.get(i - 1))));
                        this.buttonList.add(secretButtonList.get(i - 1));
                    }
                    for (int i = (int) Math.ceil((double) Waypoints.secretNum / 2) + 1; i <= Waypoints.secretNum; i++) {
                        int adjustPos = (-40 * (Waypoints.secretNum - (int) Math.ceil((double) Waypoints.secretNum / 2))) - 70 + (80 * (i-(int) Math.ceil((double) Waypoints.secretNum / 2)));
                        secretButtonList.set(i - 1, new GuiButton(10 + i, (width / 2) + adjustPos, height / 6 + 200, 60, 20, i + ": " + getOnOff(Waypoints.secretsList.get(i - 1))));
                        this.buttonList.add(secretButtonList.get(i - 1));
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        Minecraft mc = Minecraft.getMinecraft();

        String text1 = "§lDungeon Room Waypoints:";
        int text1Width = mc.fontRendererObj.getStringWidth(text1);
        TextRenderer.drawText(mc, text1, width / 2 - text1Width / 2, height / 6 - 25, 1D, false);

        String text2 = "(Use at your own risk)";
        int text2Width = mc.fontRendererObj.getStringWidth(text2);
        TextRenderer.drawText(mc, EnumChatFormatting.GRAY + text2, width / 2 - text2Width / 2, height / 6 - 15, 1D, false);

        String text3 = "Toggle Room Specific Waypoints:";
        int text3Width = mc.fontRendererObj.getStringWidth(text3);
        TextRenderer.drawText(mc, text3, width / 2 - text3Width / 2, height / 6 + 140, 1D, false);

        String text4 = "(You can also press the # key matching the secret instead)";
        int text4Width = mc.fontRendererObj.getStringWidth(text4);
        TextRenderer.drawText(mc, EnumChatFormatting.GRAY + text4, width / 2 - text4Width / 2, height / 6 + 150, 1D, false);


        if (!Utils.inCatacombs) {
            String errorText = "Not in dungeons";
            int errorTextWidth = mc.fontRendererObj.getStringWidth(errorText);
            TextRenderer.drawText(mc, EnumChatFormatting.RED + errorText, width / 2 - errorTextWidth / 2, height / 6 + 170, 1D, false);
        } else if (Waypoints.secretNum == 0) {
            String errorText = "No secrets in this room";
            int errorTextWidth = mc.fontRendererObj.getStringWidth(errorText);
            TextRenderer.drawText(mc, EnumChatFormatting.RED + errorText, width / 2 - errorTextWidth / 2, height / 6 + 170, 1D, false);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (button == waypointsEnabled) {
            Waypoints.enabled = !Waypoints.enabled;
            ConfigHandler.writeBooleanConfig("toggles", "waypointsToggled", Waypoints.enabled);
            waypointsEnabled.displayString = waypointBtnText();
            if (Waypoints.enabled) {
                player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Waypoints will now automatically show up when you enter a new dungeon room."));
            }
        } else if (button == practiceModeEnabled) {
            Waypoints.practiceModeOn = !Waypoints.practiceModeOn;
            ConfigHandler.writeBooleanConfig("waypoint", "practiceModeOn", Waypoints.practiceModeOn);
            practiceModeEnabled.displayString = "Practice Mode: " + getOnOff(Waypoints.practiceModeOn);
            if (Waypoints.practiceModeOn) {
                player.addChatMessage(new ChatComponentText("§eDungeon Rooms: Practice Mode has been enabled.\n"
                        + "§e Waypoints will ONLY show up while you are pressing \"" + GameSettings.getKeyDisplayString(DungeonRooms.keyBindings[2].getKeyCode()) + "\".\n"
                        + "§r (Hotkey is configurable in Minecraft Controls menu)"
                ));
            }
        } else if (button == showEntrance) {
            Waypoints.showEntrance = !Waypoints.showEntrance;
            ConfigHandler.writeBooleanConfig("waypoint", "showEntrance", Waypoints.showEntrance);
            showEntrance.displayString = "Show Entrance Waypoints: " + getOnOff(Waypoints.showEntrance);
        } else if (button == showSuperboom) {
            Waypoints.showSuperboom = !Waypoints.showSuperboom;
            ConfigHandler.writeBooleanConfig("waypoint", "showSuperboom", Waypoints.showSuperboom);
            showSuperboom.displayString = "Show Superboom Waypoints: " + getOnOff(Waypoints.showSuperboom);
        } else if (button == showSecrets) {
            Waypoints.showSecrets = !Waypoints.showSecrets;
            ConfigHandler.writeBooleanConfig("waypoint", "showSecrets", Waypoints.showSecrets);
            showSecrets.displayString = "Show Secret Waypoints: " + getOnOff(Waypoints.showSecrets);
        } else if (button == showFairySouls) {
            Waypoints.showFairySouls = !Waypoints.showFairySouls;
            ConfigHandler.writeBooleanConfig("waypoint", "showFairySouls", Waypoints.showFairySouls);
            showFairySouls.displayString = "Show Fairy Soul Waypoints: " + getOnOff(Waypoints.showFairySouls);
        } else if (button == showStonk) {
            Waypoints.showStonk = !Waypoints.showStonk;
            ConfigHandler.writeBooleanConfig("waypoint", "showStonk", Waypoints.showStonk);
            showStonk.displayString = "Show Stonk Waypoints: " + getOnOff(Waypoints.showStonk);
        } else if (button == sneakToDisable) {
            Waypoints.sneakToDisable = !Waypoints.sneakToDisable;
            ConfigHandler.writeBooleanConfig("waypoint", "sneakToDisable", Waypoints.sneakToDisable);
            sneakToDisable.displayString = "Double-Tap Sneak to Hide Nearby: " + getOnOff(Waypoints.sneakToDisable);
        } else if (button == disableWhenAllFound) {
            Waypoints.disableWhenAllFound = !Waypoints.disableWhenAllFound;
            ConfigHandler.writeBooleanConfig("waypoint", "disableWhenAllFound", Waypoints.disableWhenAllFound);
            disableWhenAllFound.displayString = "Disable when all secrets found: " + getOnOff(Waypoints.disableWhenAllFound);
        }
        else if (button == close) {
            player.closeScreen();
        }

        if (Utils.inCatacombs) {
            if (Waypoints.secretNum > 0) {
                for (int i = 1; i <= Waypoints.secretNum; i++) {
                    if (button == secretButtonList.get(i-1)) {
                        Waypoints.secretsList.set(i-1, !Waypoints.secretsList.get(i-1));
                        if (!RoomDetection.roomName.equals("undefined")) {
                            Waypoints.allSecretsMap.replace(RoomDetection.roomName, Waypoints.secretsList);
                        }
                        secretButtonList.get(i-1).displayString = i + ": " + getOnOff(Waypoints.secretsList.get(i - 1));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        waypointGuiOpened = false;
    }

    @Override
    protected void keyTyped(char c, int keyCode) throws IOException
    {
        super.keyTyped(c, keyCode);

        if (waypointGuiOpened && Utils.inCatacombs) {
            if (Waypoints.secretNum > 0) {
                for (int i = 1; i <= Waypoints.secretNum; i++) {
                    if (keyCode-1 == i) {
                        Waypoints.secretsList.set(i - 1, !Waypoints.secretsList.get(i - 1));
                        if (!RoomDetection.roomName.equals("undefined")) {
                            Waypoints.allSecretsMap.replace(RoomDetection.roomName, Waypoints.secretsList);
                        }
                        secretButtonList.get(i - 1).displayString = i + ": " + getOnOff(Waypoints.secretsList.get(i - 1));
                        break;
                    }
                }
            }
        }
    }

    private static String waypointBtnText() {
        if (Waypoints.enabled){
            return EnumChatFormatting.GREEN + "§lWaypoints Enabled";
        } else {
            return EnumChatFormatting.RED + "§lWaypoints Disabled";
        }
    }

    private static String getOnOff(boolean bool) {
        if (bool){
            return EnumChatFormatting.GREEN + "On";
        } else {
            return EnumChatFormatting.RED + "Off";
        }
    }
}
