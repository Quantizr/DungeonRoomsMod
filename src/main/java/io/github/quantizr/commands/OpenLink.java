package io.github.quantizr.commands;

import io.github.quantizr.DungeonRooms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class OpenLink {

    public static void checkForLink(String type){
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (!AutoRoom.chatToggled && !AutoRoom.guiToggled){
            List<String> autoText = AutoRoom.autoText();
            if (autoText != null) {
                AutoRoom.autoTextOutput = autoText;
            }
        }

        if (AutoRoom.lastRoomHash == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: You do not appear to be in a detected Dungeon room right now."));
            return;
        }
        if (AutoRoom.lastRoomJson == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: This command does not work when the current room is detected as one of multiple."));
            return;
        }
        if (AutoRoom.lastRoomJson.get("dsg").getAsString().equals("null") && AutoRoom.lastRoomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no channels/images for this room."));
            return;
        }

        switch (type) {
            case "gui":
                mc.displayGuiScreen(new LinkGUI());
                break;
            case "dsg":
                OpenLink.openDiscord("client");
                break;
            case "sbp":
                if (DungeonRooms.usingSBPSecrets) {
                    OpenLink.openSBPSecrets();
                } else {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Dungeon Rooms: You do not have the SBP Secrets Mod installed, get it from https://discord.gg/QXA3y5EbNA"));
                }
                break;
        }

    }

    public static void openDiscord(String type) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (AutoRoom.lastRoomJson.get("dsg").getAsString().equals("null")) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There is no DSG channel for this room."));
            return;
        }
        try {
            if (type.equals("client")){
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Client..."));
                Desktop.getDesktop().browse(new URI("discord://" + AutoRoom.lastRoomJson.get("dsg").getAsString()));
            } else {
                player.addChatMessage(new ChatComponentText("Dungeon Rooms: Opening DSG Discord in Browser..."));
                Desktop.getDesktop().browse(new URI("https://discord.com" + AutoRoom.lastRoomJson.get("dsg").getAsString()));
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void openSBPSecrets() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (AutoRoom.lastRoomJson.get("sbp") == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Dungeon Rooms: There are no SBP images for this room."));
            return;
        }
        String name = AutoRoom.lastRoomJson.get("sbp").getAsString();

        String category = AutoRoom.lastRoomJson.get("category").getAsString();
        switch (category) {
            case "Puzzle":
                category = "puzzles";
                break;
            case "Trap":
                category = "puzzles";
                break;
            case "L-shape":
                category = "L";
                break;
        }
        ClientCommandHandler.instance.executeCommand(FMLClientHandler.instance().getClientPlayerEntity(), "/secretoverride " + category + " " + name);
    }
}
