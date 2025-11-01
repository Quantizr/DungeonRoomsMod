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

package io.github.quantizr.dungeonrooms.video;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class VideoEventHandler {
    
    private static boolean videoFocused = false;
    
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        
        try {
            if (!VideoPlayer.isJavaFXAvailable()) {
                return;
            }
            VideoRenderer.getInstance().renderVideoOverlay();
        } catch (NoClassDefFoundError e) {
            // JavaFX classes not available - silently ignore
        }
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        
        try {
            int key = Keyboard.getEventKey();
            
            // Toggle video overlay with 'V' key
            if (key == Keyboard.KEY_V) {
                if (!VideoPlayer.isJavaFXAvailable()) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §cVideo player unavailable - JavaFX not found")
                    );
                    return;
                }
                
                VideoRenderer renderer = VideoRenderer.getInstance();
                renderer.toggleEnabled();
                
                if (renderer.isEnabled()) {
                    VideoPlayer.getInstance().play();
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §aVideo overlay enabled")
                    );
                } else {
                    VideoPlayer.getInstance().pause();
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §cVideo overlay disabled")
                    );
                }
            }
            
            // Only process video controls if JavaFX is available and overlay is enabled
            if (!VideoPlayer.isJavaFXAvailable() || !VideoRenderer.getInstance().isEnabled()) {
                return;
            }
            
            switch (key) {
                case Keyboard.KEY_SPACE:
                    VideoPlayer.getInstance().togglePlayPause();
                    break;
                case Keyboard.KEY_RIGHT:
                    VideoPlayer.getInstance().seek(10); // Skip forward 10 seconds
                    break;
                case Keyboard.KEY_LEFT:
                    VideoPlayer.getInstance().seek(-10); // Skip backward 10 seconds
                    break;
                case Keyboard.KEY_UP:
                    VideoPlayer.getInstance().adjustVolume(0.1);
                    break;
                case Keyboard.KEY_DOWN:
                    VideoPlayer.getInstance().adjustVolume(-0.1);
                    break;
                case Keyboard.KEY_EQUALS: // + key
                case Keyboard.KEY_ADD:
                    VideoRenderer.getInstance().adjustOpacity(0.1f);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §eOpacity: " + 
                            String.format("%.1f", VideoRenderer.getInstance().getOpacity()))
                    );
                    break;
                case Keyboard.KEY_MINUS:
                case Keyboard.KEY_SUBTRACT:
                    VideoRenderer.getInstance().adjustOpacity(-0.1f);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §eOpacity: " + 
                            String.format("%.1f", VideoRenderer.getInstance().getOpacity()))
                    );
                    break;
            }
        } catch (NoClassDefFoundError e) {
            // JavaFX classes not available - silently ignore
        }
    }
}
