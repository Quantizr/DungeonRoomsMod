/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.quantizr.core.AutoRoom;
import io.github.quantizr.commands.DungeonRoomCommand;
import io.github.quantizr.core.Waypoints;
import io.github.quantizr.gui.LinkGUI;
import io.github.quantizr.gui.WaypointsGUI;
import io.github.quantizr.handlers.OpenLink;
import io.github.quantizr.handlers.ConfigHandler;
import io.github.quantizr.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = DungeonRooms.MODID, version = DungeonRooms.VERSION)
public class DungeonRooms
{
    public static final String MODID = "dungeonrooms";
    public static final String VERSION = "2.1.0";

    Minecraft mc = Minecraft.getMinecraft();
    public static Logger logger;

    public static JsonObject roomsJson;
    public static JsonObject waypointsJson;
    static boolean updateChecked = false;
    public static boolean usingSBPSecrets = false;
    public static String guiToOpen = null;
    public static KeyBinding[] keyBindings = new KeyBinding[2];
    public static String hotkeyOpen = "gui";
    static int tickAmount = 1;
    public static List<String> motd = null;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DungeonRoomCommand());
        logger = LogManager.getLogger("DungeonRooms");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AutoRoom());
        MinecraftForge.EVENT_BUS.register(new Waypoints());

        ConfigHandler.reloadConfig();

        try {
            ResourceLocation roomsLoc = new ResourceLocation( "dungeonrooms","dungeonrooms.json");
            InputStream roomsIn = Minecraft.getMinecraft().getResourceManager().getResource(roomsLoc).getInputStream();
            BufferedReader roomsReader = new BufferedReader(new InputStreamReader(roomsIn));

            ResourceLocation waypointsLoc = new ResourceLocation( "dungeonrooms","secretlocations.json");
            InputStream waypointsIn = Minecraft.getMinecraft().getResourceManager().getResource(waypointsLoc).getInputStream();
            BufferedReader waypointsReader = new BufferedReader(new InputStreamReader(waypointsIn));

            Gson gson = new Gson();
            roomsJson = gson.fromJson(roomsReader, JsonObject.class);
            logger.info("DungeonRooms: Loaded dungeonrooms.json");

            waypointsJson = gson.fromJson(waypointsReader, JsonObject.class);
            logger.info("DungeonRooms: Loaded secretlocations.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        keyBindings[0] = new KeyBinding("Open Room Images in DSG/SBP", Keyboard.KEY_O, "Dungeon Rooms Mod");
        keyBindings[1] = new KeyBinding("Open Waypoint Menu", Keyboard.KEY_P, "Dungeon Rooms Mod");
        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        usingSBPSecrets = Loader.isModLoaded("sbp");
        DungeonRooms.logger.info("DungeonRooms: SBP Dungeon Secrets detection: " + usingSBPSecrets);
    }

    /*
    Update Checker taken from Danker's Skyblock Mod (https://github.com/bowser0000/SkyblockMod/).
    This code was released under GNU General Public License v3.0 and remains under said license.
    Modified by Quantizr (_risk) in Feb. 2021.
    */
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (!updateChecked) {
            updateChecked = true;

            // MULTI THREAD DRIFTING
            new Thread(() -> {
                try {
                    DungeonRooms.logger.info("DungeonRooms: Checking for updates...");

                    URL url = new URL("https://api.github.com/repos/Quantizr/DungeonRoomsMod/releases/latest");
                    URLConnection request = url.openConnection();
                    request.connect();
                    JsonParser json = new JsonParser();
                    JsonObject latestRelease = json.parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

                    String latestTag = latestRelease.get("tag_name").getAsString();
                    DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(VERSION);
                    DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

                    if (currentVersion.compareTo(latestVersion) < 0) {
                        String releaseURL = "https://discord.gg/kr2M7WutgJ";
                        ChatComponentText update = new ChatComponentText(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "  [UPDATE]  ");
                        update.setChatStyle(update.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)));

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dungeon Rooms Mod is outdated. Please update to " + latestTag + ".\n").appendSibling(update));
                    }
                } catch (IOException e) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured. See logs for more details."));
                    e.printStackTrace();
                }

                try {
                    URL url = new URL("https://gist.githubusercontent.com/Quantizr/0af2afd91cd8b1aa22e42bc2d65cfa75/raw/");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    String line;
                    motd = new ArrayList<>();
                    while ((line = in.readLine()) != null) {
                        motd.add(line);
                    }
                    in.close();
                } catch (IOException e) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured. See logs for more details."));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (Utils.inDungeons) {
            if (AutoRoom.guiToggled) {
                AutoRoom.renderText();
            }
            if (AutoRoom.coordToggled) {
                AutoRoom.renderCoord();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;

        tickAmount++;

        // Checks every second
        if (tickAmount % 20 == 0) {
            if (player != null) {
                Utils.checkForSkyblock();
                Utils.checkForDungeons();
                tickAmount = 0;
            }
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (keyBindings[0].isPressed()) {
            if (!Utils.inDungeons) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Dungeon Rooms: Use this hotkey in dungeons"));
                return;
            }
            switch (hotkeyOpen) {
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
            DungeonRooms.guiToOpen = "waypoints";
        }
    }

    // Delay GUI by 1 tick
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (guiToOpen != null) {
            switch (guiToOpen) {
                case "link":
                    mc.displayGuiScreen(new LinkGUI());
                    break;
                case "waypoints":
                    mc.displayGuiScreen(new WaypointsGUI());
                    break;
            }
            guiToOpen = null;
        }
    }
}
