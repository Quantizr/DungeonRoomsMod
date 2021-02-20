/*
Copyright 2021 Quantizr(_risk)
This file is used as part of Dungeon Rooms Mod (DRM). (Github: <https://github.com/Quantizr/DungeonRoomsMod>)
DRM  is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
DRM is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with DRM.  If not, see <https://www.gnu.org/licenses/>.
*/

package io.github.quantizr;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.quantizr.commands.AutoRoom;
import io.github.quantizr.commands.DungeonRoomCommand;
import io.github.quantizr.commands.OpenLink;
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
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Mod(modid = DungeonRooms.MODID, version = DungeonRooms.VERSION)
public class DungeonRooms
{
    public static final String MODID = "dungeonrooms";
    public static final String VERSION = "1.0.1";

    Minecraft mc = Minecraft.getMinecraft();

    public static JsonObject roomsJson;
    static boolean updateChecked = false;
    public static boolean usingSBPSecrets = false;
    static KeyBinding[] keyBindings = new KeyBinding[1];
    public static String hotkeyOpen = "gui";
    static int tickAmount = 1;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DungeonRoomCommand());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AutoRoom());

        ConfigHandler.reloadConfig();

        try {
            ResourceLocation loc = new ResourceLocation( "dungeonrooms","dungeonrooms.json");
            InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            Gson gson = new Gson();
            roomsJson = gson.fromJson(reader, JsonObject.class);
            System.out.println("Loaded dungeonrooms.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyBindings[0] = new KeyBinding("Open Room Images in DSG/SBP", Keyboard.KEY_O, "Dungeon Rooms Mod");
        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        usingSBPSecrets = Loader.isModLoaded("dgnscrts");
        System.out.println("SBP Dungeon Secrets detection: " + usingSBPSecrets);
    }

    /*
    Update Checker taken from Danker's Skyblock Mod (https://github.com/bowser0000/SkyblockMod/).
    This code was released under GNU General Public License v3.0 and remains under said license.
    Modified by Quantizr (_risk) in Feb. 2021.
    */
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (!updateChecked) {
            updateChecked = true;

            // MULTI THREAD DRIFTING
            new Thread(() -> {
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                try {
                    System.out.println("Checking for updates...");

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
            }).start();
        }
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (Utils.inDungeons && AutoRoom.guiToggled) {
            AutoRoom.renderText();
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
                    player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord..."));
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
    }
}
