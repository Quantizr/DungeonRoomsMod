# Video Player Feature Guide

## Overview
The video player overlay allows you to watch videos while playing Minecraft dungeons. It uses JavaFX (built into Java 8) to render video content over the game.

## Files Created/Modified

### New Files:
1. **VideoPlayer.java** - Singleton managing JavaFX MediaPlayer
2. **VideoRenderer.java** - OpenGL overlay rendering with DynamicTexture
3. **VideoEventHandler.java** - Forge event handler for rendering and input
4. **VideoPlayerGUI.java** - GUI with file browser and playback controls

### Modified Files:
1. **DungeonRooms.java** - Added video event handler registration and keybinding

## Controls

### Opening the Video Player GUI:
- Press **U** (default keybinding)
- Configurable in Minecraft Controls menu under "Dungeon Rooms Mod"

### Video Overlay Controls:
- **V** - Toggle video overlay on/off
- **Space** - Play/Pause
- **Left Arrow** - Seek backward 5 seconds
- **Right Arrow** - Seek forward 5 seconds
- **Up Arrow** - Increase volume
- **Down Arrow** - Decrease volume
- **+** (Plus/Equals key) - Increase opacity
- **-** (Minus key) - Decrease opacity

## Usage

1. **Load a Video:**
   - Press **U** to open the Video Player GUI
   - Click "Browse" to select a video file OR manually enter a file path
   - Supported formats: MP4, AVI, MKV, MOV, FLV, WMV
   - Click "Load Video"

2. **Control Playback:**
   - Use the GUI buttons OR close the GUI and use keyboard shortcuts
   - The video will loop automatically when it reaches the end

3. **Toggle Overlay:**
   - Press **V** to show/hide the video while playing
   - Video continues playing in the background when hidden

## Technical Details

- **Video Size:** Default 854x480 pixels (centered on screen)
- **Opacity:** Default 0.6 (60% transparent), adjustable 0.1-1.0
- **Performance:** Uses hardware-accelerated JavaFX rendering
- **Thread Safety:** All JavaFX operations wrapped in Platform.runLater
- **Looping:** Videos automatically loop indefinitely

## Building

The video player is fully integrated. Simply build the mod as usual:
```
.\gradlew.bat build
```

The compiled mod will be in: `build\libs\dungeonrooms-1.0.jar`

## Tips

1. **Pre-download Videos:** Download videos locally for best performance (no streaming)
2. **Adjust Opacity:** Use +/- keys to find the right transparency for your needs
3. **Practice Mode:** Works great with waypoint practice mode - watch tutorials while learning!
4. **Keybind Conflicts:** Check Minecraft Controls menu if keys don't work

## Troubleshooting

**Video won't load:**
- Ensure file path is correct
- Check file format is supported
- Try a different video file

**Overlay not showing:**
- Press V to toggle overlay
- Check if video is actually loaded and playing
- Look for chat messages indicating video state

**Performance issues:**
- Use smaller video resolutions (720p or lower recommended)
- Close video player when not in use
- Lower video opacity can improve visibility with less distraction

## Party Coordination Integration

This video player works alongside the party coordination features:
- SS-FOUND messages won't interfere with video overlay
- Video continues playing while in dungeons
- Perfect for watching during repetitive farming sessions

Enjoy your video overlay! 🎥
