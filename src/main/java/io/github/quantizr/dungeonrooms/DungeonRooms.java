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

package io.github.quantizr.dungeonrooms;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.quantizr.dungeonrooms.commands.RoomCommand;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.Waypoints;
import io.github.quantizr.dungeonrooms.gui.WaypointsGUI;
import io.github.quantizr.dungeonrooms.handlers.ConfigHandler;
import io.github.quantizr.dungeonrooms.handlers.OpenLink;
import io.github.quantizr.dungeonrooms.handlers.PacketHandler;
import io.github.quantizr.dungeonrooms.handlers.TextRenderer;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.DungeonManager;
import io.github.quantizr.dungeonrooms.dungeons.catacombs.RoomDetection;
import io.github.quantizr.dungeonrooms.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

@Mod(modid = DungeonRooms.MODID, version = DungeonRooms.VERSION)
public class DungeonRooms
{
    public static final String MODID = "dungeonrooms";
    public static final String VERSION = "3.3.2";

    Minecraft mc = Minecraft.getMinecraft();
    public static Logger logger;

    public static JsonObject roomsJson;
    public static JsonObject waypointsJson;
    public static HashMap<String,HashMap<String,long[]>> ROOM_DATA = new HashMap<>();

    public static boolean usingSBPSecrets = false;
    public static KeyBinding[] keyBindings = new KeyBinding[3];
    public static String imageHotkeyOpen = "gui";
    static int tickAmount = 1;

    public static List<String> textToDisplay = null;
    public static int textLocX = 50;
    public static int textLocY = 5;

    public static List<String> motd = null;
    public static String configDir;
    public static boolean firstLogin = false;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new RoomCommand());
        configDir = event.getModConfigurationDirectory().toString();

        //initialize logger
        logger = LogManager.getLogger(DungeonRooms.class);
        Utils.setLogLevel(LogManager.getLogger(DungeonRooms.class), Level.INFO);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        long time1 = System.currentTimeMillis();

        //start room data loading executors first or else it will block later and slow down loading by ~200ms
        List<Path> paths = Utils.getAllPaths("catacombs");
        final ExecutorService executor = Executors.newFixedThreadPool(4); //don't need 8 threads cause it's just 1x1 that takes longest
        Future<HashMap<String, long[]>> future1x1 = executor.submit(() -> Utils.pathsToRoomData("1x1", paths));
        Future<HashMap<String, long[]>> future1x2 = executor.submit(() -> Utils.pathsToRoomData("1x2", paths));
        Future<HashMap<String, long[]>> future1x3 = executor.submit(() -> Utils.pathsToRoomData("1x3", paths));
        Future<HashMap<String, long[]>> future1x4 = executor.submit(() -> Utils.pathsToRoomData("1x4", paths));
        Future<HashMap<String, long[]>> future2x2 = executor.submit(() -> Utils.pathsToRoomData("2x2", paths));
        Future<HashMap<String, long[]>> futureLShape = executor.submit(() -> Utils.pathsToRoomData("L-shape", paths));
        Future<HashMap<String, long[]>> futurePuzzle = executor.submit(() -> Utils.pathsToRoomData("Puzzle", paths));
        Future<HashMap<String, long[]>> futureTrap = executor.submit(() -> Utils.pathsToRoomData("Trap", paths));

        //register classes
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DungeonManager());
        MinecraftForge.EVENT_BUS.register(new RoomDetection());
        MinecraftForge.EVENT_BUS.register(new Waypoints());

        //reload config
        ConfigHandler.reloadConfig();

        //register keybindings
        keyBindings[0] = new KeyBinding("Open Room Images in DSG/SBP", Keyboard.KEY_O, "Dungeon Rooms Mod");
        keyBindings[1] = new KeyBinding("Open Waypoint Config Menu", Keyboard.KEY_P, "Dungeon Rooms Mod");
        keyBindings[2] = new KeyBinding("Show Waypoints in Practice Mode", Keyboard.KEY_I, "Dungeon Rooms Mod");
        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }

        //get room and waypoint info
        try (BufferedReader roomsReader = new BufferedReader(new InputStreamReader(mc.getResourceManager()
                .getResource(new ResourceLocation("dungeonrooms", "dungeonrooms.json")).getInputStream()));
            BufferedReader waypointsReader = new BufferedReader(new InputStreamReader(mc.getResourceManager()
                .getResource(new ResourceLocation("dungeonrooms", "secretlocations.json")).getInputStream()))
        ) {
            Gson gson = new Gson();
            roomsJson = gson.fromJson(roomsReader, JsonObject.class);
            logger.info("DungeonRooms: Loaded dungeonrooms.json");

            waypointsJson = gson.fromJson(waypointsReader, JsonObject.class);
            logger.info("DungeonRooms: Loaded secretlocations.json");
        } catch (IOException e) {
            e.printStackTrace();
        }


        //set RoomData to futures - this will block if the rest of init was fast
        try {
            long time2 = System.currentTimeMillis();
            ROOM_DATA.put("1x1", future1x1.get());
            long time3 = System.currentTimeMillis();
            ROOM_DATA.put("1x2", future1x2.get());
            ROOM_DATA.put("1x3", future1x3.get());
            ROOM_DATA.put("1x4", future1x4.get());
            ROOM_DATA.put("2x2", future2x2.get());
            ROOM_DATA.put("L-shape", futureLShape.get());
            ROOM_DATA.put("Puzzle", futurePuzzle.get());
            ROOM_DATA.put("Trap", futureTrap.get());
            long time4 = System.currentTimeMillis();
            logger.debug("DungeonRooms: Time(ms) for init before get futures: " + (time2 - time1));
            logger.debug("DungeonRooms: Blocked Time(ms) for 1x1: " + (time3 - time2));
            logger.debug("DungeonRooms: Blocked Time(ms) remaining for other rooms: " + (time4 - time3));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        usingSBPSecrets = Loader.isModLoaded("sbp");
        DungeonRooms.logger.info("DungeonRooms: SBP Dungeon Secrets detection: " + usingSBPSecrets);
    }

    /**
     * Modified from Danker's Skyblock Mod under the GNU General Public License v3.0
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc.getCurrentServerData() == null) return;
        if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.")) {
            logger.info("DungeonRooms: Connecting to Hypixel...");

            //Packets are used in this mod solely to detect when the player picks up an item. No packets are modified or created.
            event.manager.channel().pipeline().addBefore("packet_handler", "drm_packet_handler", new PacketHandler());
            logger.info("DungeonRooms: Packet Handler added");

            new Thread(() -> {
                try {
                    while (mc.thePlayer == null) {
                        //Yes, I'm too lazy to code something proper so I'm busy-waiting, shut up. no :) -carmel
                        //It usually waits for less than half a second
                        Thread.sleep(100);
                    }
                    Thread.sleep(3000);
                    if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.")) {
                        logger.info("DungeonRooms: Checking for conflicting keybindings...");
                        Utils.checkForConflictingHotkeys();

                        logger.info("DungeonRooms: Checking for updates...");
                        URL url = new URL("https://api.github.com/repos/Quantizr/DungeonRoomsMod/releases/latest");
                        URLConnection request = url.openConnection();
                        request.connect();
                        JsonParser json = new JsonParser();
                        JsonObject latestRelease = json.parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

                        String latestTag = latestRelease.get("tag_name").getAsString();
                        DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(VERSION);
                        DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

                        if (currentVersion.compareTo(latestVersion) < 0) {
                            String releaseURL = "https://github.com/Quantizr/DungeonRoomsMod/releases/latest";
                            ChatComponentText update = new ChatComponentText(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "  [UPDATE]  ");
                            update.setChatStyle(update.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)));
                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dungeon Rooms Mod is outdated. Please update to " + latestTag + ".\n").appendSibling(update));
                        } else {
                            logger.info("DungeonRooms: No update found");
                        }

                        logger.info("DungeonRooms: Getting MOTD...");
                        url = new URL("https://gist.githubusercontent.com/Quantizr/01aca53e61cef5dfd08989fec600b204/raw/");
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                        String line;
                        motd = new ArrayList<>();
                        while ((line = in.readLine()) != null) {
                            motd.add(line);
                        }
                        in.close();
                        logger.info("DungeonRooms: MOTD has been checked");
                    }
                } catch (IOException | InterruptedException e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dungeon Rooms: An error has occured. See logs for more details."));
                    e.printStackTrace();
                }

            }).start();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        EntityPlayerSP player = mc.thePlayer;

        tickAmount++;
        if (tickAmount % 20 == 0) {
            if (player != null) {
                Utils.checkForSkyblock();
                Utils.checkForCatacombs();
                tickAmount = 0;
            }
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (keyBindings[0].isPressed()) {
            if (!Utils.inCatacombs) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: Use this hotkey inside of a dungeon room"));
                return;
            }
            switch (imageHotkeyOpen) {
                case "gui":
                    OpenLink.checkForLink("gui");
                    break;
                case "dsg":
                    OpenLink.checkForLink("dsg");
                    break;
                case "sbp":
                    OpenLink.checkForLink("sbp");
                    break;
                default:
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: hotkeyOpen config value improperly set, do \"/room set <gui | dsg | sbp>\" to change the value"));
                    break;
            }
        }
        if (keyBindings[1].isPressed()) {
            mc.addScheduledTask(() -> mc.displayGuiScreen(new WaypointsGUI()));
        }
        if (keyBindings[2].isPressed()) {
            if (Waypoints.enabled && !Waypoints.practiceModeOn) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: Run \"/room toggle practice\" to enable Practice Mode."));
            } else if (!Waypoints.enabled && Waypoints.practiceModeOn) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: Waypoints must be enabled for Practice Mode to work."));
            }
        }
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (Utils.inSkyblock) {
            if (textToDisplay != null && !textToDisplay.isEmpty()) {
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                int y = 0;
                for (String line:textToDisplay) {
                    int roomStringWidth = mc.fontRendererObj.getStringWidth(line);
                    TextRenderer.drawText(mc, line, ((scaledResolution.getScaledWidth() * textLocX) / 100) - (roomStringWidth / 2),
                            ((scaledResolution.getScaledHeight() * textLocY) / 100) + y, 1D, true);
                    y += mc.fontRendererObj.FONT_HEIGHT;
                }
            }
        }
    }
}
