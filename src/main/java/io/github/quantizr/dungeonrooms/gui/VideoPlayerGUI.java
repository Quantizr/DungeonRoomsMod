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

import io.github.quantizr.dungeonrooms.video.VideoPlayer;
import io.github.quantizr.dungeonrooms.video.VideoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class VideoPlayerGUI extends GuiScreen {
    private GuiTextField videoPathField;
    private GuiButton loadButton;
    private GuiButton browseButton;
    private GuiButton playPauseButton;
    private GuiButton closeButton;
    
    private String selectedVideoPath = "";
    
    @Override
    public void initGui() {
        super.initGui();
        
        // Check if JavaFX is available
        if (!VideoPlayer.isJavaFXAvailable()) {
            return; // GUI will show error message in drawScreen
        }
        
        int centerX = this.width / 2;
        int startY = this.height / 4;
        
        // Video path text field
        videoPathField = new GuiTextField(0, this.fontRendererObj, centerX - 150, startY, 300, 20);
        videoPathField.setMaxStringLength(500);
        videoPathField.setText(selectedVideoPath);
        
        // Browse button
        browseButton = new GuiButton(1, centerX - 150, startY + 30, 70, 20, "Browse...");
        this.buttonList.add(browseButton);
        
        // Load button
        loadButton = new GuiButton(2, centerX - 70, startY + 30, 70, 20, "Load Video");
        this.buttonList.add(loadButton);
        
        // Play/Pause button
        playPauseButton = new GuiButton(3, centerX + 10, startY + 30, 70, 20, 
            VideoPlayer.getInstance().isPlaying() ? "Pause" : "Play");
        this.buttonList.add(playPauseButton);
        
        // Close button
        closeButton = new GuiButton(4, centerX - 50, startY + 80, 100, 20, "Close");
        this.buttonList.add(closeButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) { // Browse
            openFileBrowser();
        } else if (button.id == 2) { // Load Video
            String path = videoPathField.getText().trim();
            if (!path.isEmpty()) {
                VideoPlayer.getInstance().loadVideo(path);
                mc.thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText("§6[Dungeon Rooms] §aLoading video...")
                );
            }
        } else if (button.id == 3) { // Play/Pause
            VideoPlayer.getInstance().togglePlayPause();
            playPauseButton.displayString = VideoPlayer.getInstance().isPlaying() ? "Pause" : "Play";
        } else if (button.id == 4) { // Close
            mc.displayGuiScreen(null);
        }
    }
    
    private void openFileBrowser() {
        // Run file chooser on a separate thread to avoid blocking
        new Thread(() -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Video File");
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Video Files", "mp4", "avi", "mkv", "mov", "flv", "wmv"));
                
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    selectedVideoPath = selectedFile.getAbsolutePath();
                    videoPathField.setText(selectedVideoPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        videoPathField.textboxKeyTyped(typedChar, keyCode);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        videoPathField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        // Title
        this.drawCenteredString(this.fontRendererObj, "§6§lDungeon Rooms - Video Player", 
            this.width / 2, this.height / 4 - 40, 0xFFFFFF);
        
        // Check if JavaFX is available
        if (!VideoPlayer.isJavaFXAvailable()) {
            this.drawCenteredString(this.fontRendererObj, "§c§lJavaFX Not Available", 
                this.width / 2, this.height / 2 - 20, 0xFF5555);
            this.drawCenteredString(this.fontRendererObj, "§eThe video player requires JavaFX which is not available", 
                this.width / 2, this.height / 2, 0xFFFFFF);
            this.drawCenteredString(this.fontRendererObj, "§ein your current Java installation.", 
                this.width / 2, this.height / 2 + 15, 0xFFFFFF);
            this.drawCenteredString(this.fontRendererObj, "§7Press ESC to close", 
                this.width / 2, this.height / 2 + 40, 0xAAAAAA);
            return;
        }
        
        // Instructions
        this.drawCenteredString(this.fontRendererObj, "§eControls: V=Toggle | Space=Play/Pause | Arrows=Seek/Volume | +/-=Opacity", 
            this.width / 2, this.height / 4 - 20, 0xFFFFFF);
        
        // Video path label
        this.drawString(this.fontRendererObj, "Video Path:", this.width / 2 - 150, 
            this.height / 4 - 12, 0xFFFFFF);
        
        // Status
        String status = "Status: " + (VideoRenderer.getInstance().isEnabled() ? "§aEnabled" : "§cDisabled");
        if (VideoPlayer.getInstance().isInitialized()) {
            status += " | " + (VideoPlayer.getInstance().isPlaying() ? "§aPlaying" : "§ePaused");
        }
        this.drawCenteredString(this.fontRendererObj, status, 
            this.width / 2, this.height / 4 + 60, 0xFFFFFF);
        
        videoPathField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
