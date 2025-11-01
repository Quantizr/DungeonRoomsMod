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

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class VideoPlayer {
    private static VideoPlayer instance;
    private static boolean javafxAvailable = false;
    
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private JFXPanel fxPanel;
    private boolean initialized = false;
    private String currentVideoPath;
    
    static {
        // Check if JavaFX is available
        try {
            Class.forName("javafx.application.Platform");
            javafxAvailable = true;
        } catch (ClassNotFoundException e) {
            System.err.println("JavaFX is not available - Video player will be disabled");
            javafxAvailable = false;
        }
    }
    
    public static boolean isJavaFXAvailable() {
        return javafxAvailable;
    }
    
    public static VideoPlayer getInstance() {
        if (instance == null) {
            instance = new VideoPlayer();
        }
        return instance;
    }
    
    private VideoPlayer() {
        if (!javafxAvailable) {
            return;
        }
        try {
            // Initialize JavaFX
            new JFXPanel(); // This initializes the JavaFX toolkit
        } catch (Exception e) {
            System.err.println("Failed to initialize JavaFX: " + e.getMessage());
            javafxAvailable = false;
        }
    }
    
    public void loadVideo(String filePath) {
        if (!javafxAvailable) {
            return;
        }
        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            System.err.println("Video file not found: " + filePath);
            return;
        }
        
        currentVideoPath = filePath;
        
        Platform.runLater(() -> {
            try {
                // Dispose of old media player if exists
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }
                
                Media media = new Media(videoFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                
                // Configure media player
                mediaPlayer.setAutoPlay(false);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop video
                mediaPlayer.setVolume(0.5);
                
                // Create media view if not exists
                if (mediaView == null) {
                    mediaView = new MediaView(mediaPlayer);
                    mediaView.setPreserveRatio(true);
                    
                    StackPane root = new StackPane();
                    root.getChildren().add(mediaView);
                    
                    Scene scene = new Scene(root);
                    
                    if (fxPanel == null) {
                        fxPanel = new JFXPanel();
                    }
                    fxPanel.setScene(scene);
                } else {
                    mediaView.setMediaPlayer(mediaPlayer);
                }
                
                initialized = true;
                System.out.println("Video loaded: " + filePath);
            } catch (Exception e) {
                System.err.println("Error loading video: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public void play() {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> mediaPlayer.play());
    }
    
    public void pause() {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> mediaPlayer.pause());
    }
    
    public void togglePlayPause() {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        });
    }
    
    public void seek(double seconds) {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> {
            Duration current = mediaPlayer.getCurrentTime();
            Duration newTime = current.add(Duration.seconds(seconds));
            mediaPlayer.seek(newTime);
        });
    }
    
    public void setVolume(double volume) {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume))));
    }
    
    public void adjustVolume(double delta) {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return;
        }
        Platform.runLater(() -> {
            double newVolume = mediaPlayer.getVolume() + delta;
            mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, newVolume)));
        });
    }
    
    public boolean isInitialized() {
        return javafxAvailable && initialized;
    }
    
    public boolean isPlaying() {
        if (!javafxAvailable || mediaPlayer == null || !initialized) {
            return false;
        }
        return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
    
    public JFXPanel getFxPanel() {
        return fxPanel;
    }
    
    public MediaView getMediaView() {
        return mediaView;
    }
    
    public void dispose() {
        if (!javafxAvailable || mediaPlayer == null) {
            return;
        }
        Platform.runLater(() -> {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        });
        initialized = false;
    }
}
