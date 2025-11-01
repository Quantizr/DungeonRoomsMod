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

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class VideoRenderer {
    private static VideoRenderer instance;
    private DynamicTexture videoTexture;
    private WritableImage fxImage;
    private BufferedImage bufferedImage;
    
    private float opacity = 0.6f;
    private int width = 854;  // Default 480p width
    private int height = 480; // Default 480p height
    
    private boolean enabled = false;
    
    public static VideoRenderer getInstance() {
        if (instance == null) {
            instance = new VideoRenderer();
        }
        return instance;
    }
    
    private VideoRenderer() {
        // Initialize
    }
    
    public void renderVideoOverlay() {
        if (!enabled || !VideoPlayer.isJavaFXAvailable()) {
            return;
        }
        
        VideoPlayer player = VideoPlayer.getInstance();
        if (!player.isInitialized()) {
            return;
        }
        
        try {
            // Capture frame from JavaFX MediaView
            captureFrame();
            
            if (videoTexture != null) {
                Minecraft mc = Minecraft.getMinecraft();
                ScaledResolution sr = new ScaledResolution(mc);
                
                int screenWidth = sr.getScaledWidth();
                int screenHeight = sr.getScaledHeight();
                
                // Center the video
                int x = (screenWidth - width) / 2;
                int y = (screenHeight - height) / 2;
                
                // Set up OpenGL for overlay rendering
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0f, 1.0f, 1.0f, opacity);
                
                // Bind and draw texture
                mc.getTextureManager().bindTexture(mc.getTextureManager().getDynamicTextureLocation("video_overlay", videoTexture));
                
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
                worldrenderer.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        } catch (Exception e) {
            // Silently fail to avoid spam
        }
    }
    
    private void captureFrame() {
        try {
            if (VideoPlayer.getInstance().getMediaView() != null) {
                javafx.application.Platform.runLater(() -> {
                    try {
                        if (fxImage == null) {
                            fxImage = new WritableImage(width, height);
                        }
                        
                        SnapshotParameters params = new SnapshotParameters();
                        VideoPlayer.getInstance().getMediaView().snapshot(params, fxImage);
                        
                        // Convert to BufferedImage
                        bufferedImage = SwingFXUtils.fromFXImage(fxImage, bufferedImage);
                        
                        // Update texture
                        if (videoTexture == null) {
                            videoTexture = new DynamicTexture(bufferedImage);
                        } else {
                            videoTexture.updateDynamicTexture();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                });
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
    
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.1f, Math.min(1.0f, opacity));
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    public void adjustOpacity(float delta) {
        setOpacity(opacity + delta);
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        fxImage = null; // Reset image to force recreation
    }
}
