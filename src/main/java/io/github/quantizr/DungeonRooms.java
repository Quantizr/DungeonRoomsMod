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
import io.github.quantizr.commands.AutoRoom;
import io.github.quantizr.commands.DungeonRoomCommand;
import io.github.quantizr.commands.OpenLink;
import io.github.quantizr.handlers.ConfigHandler;
import io.github.quantizr.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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
import org.lwjgl.input.Keyboard;

import java.io.*;

@Mod(modid = DungeonRooms.MODID, version = DungeonRooms.VERSION)
public class DungeonRooms
{
    public static final String MODID = "dungeonrooms";
    public static final String VERSION = "1.0.0";

    Minecraft mc = Minecraft.getMinecraft();

    public static JsonObject roomsJson;
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
