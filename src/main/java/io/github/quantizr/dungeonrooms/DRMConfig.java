package io.github.quantizr.dungeonrooms;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;

public class DRMConfig extends Config {
    public DRMConfig() {
        super(new Mod("DungeonsRoomsMod", ModType.SKYBLOCK, "/drm_logo_x128.png"), DungeonRooms.MODID + ".json");
        initialize();
        this.addListener("practiceModeOn", () -> {
            String text = "§eDungeon Rooms: Practice Mode has been enabled.\n§e Waypoints will ONLY show up while you are pressing " + practiceModeKeyBind.getDisplay() + "\n§r (Hotkey is configurable in Minecraft Controls menu)";
            ChatTransmitter.addToQueue(text);
        });
    }
    @Switch(name = "Display Room Names In Gui", subcategory = "General")
    public static boolean guiToggled = true;


    @Switch(name = "Display Welcome/MOTD", subcategory = "General")
    public static boolean motdToggled = true;
    @KeyBind(
            name = "Quick Access Gui Key",
            subcategory = "General"
    )
    public static OneKeyBind waypointGuiKey = new OneKeyBind(UKeyboard.KEY_P);


    @Dropdown(
            name = "Secret Image Provider",
            options = {"gui", "dsg", "sbp"},
            subcategory = "Secret Images"
    )
    public static int imageHotkeyOpen = 0;
    @KeyBind(
            name = "Keybinding For Opening Room Images from DSG/SBP",
            size = 2,
            subcategory = "Secret Images"
    )
    public static OneKeyBind openSecretImages = new OneKeyBind(UKeyboard.KEY_O);

    @Switch(
            name = "Waypoints Enabled",
            category = "Waypoints",
            subcategory = "Preferences",
            size = 2
    )
    public static boolean waypointsEnabled = true;
    @Switch(
            name = "Double-Tap Sneak to Hide Nearby",
            category = "Waypoints",
            subcategory = "Preferences"
    )
    public static boolean sneakToDisable = true;

    @Switch(
            name = "Disable when all secrets found",
            category = "Waypoints",
            subcategory = "Preferences"
    )
    public static boolean disableWhenAllFound = true;

    @Info(
            text = "See Waypoints when key is pressed, useful for practice",
            category = "Waypoints",
            subcategory = "Practice Mode",
            size = 2,
            type = InfoType.INFO
    )
    boolean dummy = false;

    @KeyBind(
            name = "Keybinding For Practice Mode",
            category = "Waypoints",
            subcategory = "Practice Mode"
    )
    public static OneKeyBind practiceModeKeyBind = new OneKeyBind(UKeyboard.KEY_I);

    @Switch(
            name = "Practice Mode",
            category = "Waypoints",
            subcategory = "Practice Mode"
    )
    public static boolean practiceModeOn = false;


    @Checkbox(
            name = "Show Entrance Waypoints",
            category = "Waypoints",
            subcategory = "Visibility"
    )
    public static boolean showEntrance = true;

    @Checkbox(
            name = "Show Superboom Waypoints",
            category = "Waypoints",
            subcategory = "Visibility"
    )
    public static boolean showSuperboom = true;

    @Checkbox(
            name = "Show Secret Waypoints",
            category = "Waypoints",
            subcategory = "Visibility"
    )
    public static boolean showSecrets = true;


    @Checkbox(
            name = "Show Fairy Soul Waypoints",
            category = "Waypoints",
            subcategory = "Visibility"
    )
    public static boolean showFairySouls = true;

    @Checkbox(
            name = "Show Stonk Waypoints",
            category = "Waypoints",
            subcategory = "Visibility"
    )
    public static boolean showStonk = true;


    @Switch(
            name = "Show Waypoint Text",
            category = "Waypoints",
            subcategory = "Appearance"
    )
    public static boolean showWaypointText = true;

    @Switch(
            name = "Show Bounding Box",
            category = "Waypoints",
            subcategory = "Appearance"
    )
    public static boolean showBoundingBox = true;

    @Switch(
            name = "Show Beacon",
            category = "Waypoints",
            subcategory = "Appearance"
    )
    public static boolean showBeacon = true;


    @Slider(name = "Offset x", min = 0, max = 100, subcategory = "Hud")
    public static int textLocX = 50;

    @Slider(name = "Offset y", min = 0, max = 100, subcategory = "Hud")
    public static int textLocY = 5;


}
